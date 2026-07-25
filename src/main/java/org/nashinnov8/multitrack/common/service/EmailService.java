package org.nashinnov8.multitrack.common.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  @Value("${BREVO_API_KEY:}")
  private String brevoApiKey;

  @Value("${RESEND_API_KEY:${MAIL_PASSWORD:}}")
  private String resendApiKey;

  @Value("${MAIL_FROM:onboarding@resend.dev}")
  private String fromEmail;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  private final RestClient resendClient = RestClient.builder()
      .baseUrl("https://api.resend.com")
      .build();

  private final RestClient brevoClient = RestClient.builder()
      .baseUrl("https://api.brevo.com/v3")
      .build();

  @Async
  public void sendVerificationEmail(String toEmail, String token) {
    log.info("[EMAIL-DEBUG] Starting sendVerificationEmail to recipient: {}", toEmail);

    String verifyUrl = frontendUrl + "/verify-email?token=" + token;
    String subject = "Verify your Multitrack account";
    
    String htmlContent = """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
          <div style="text-align: center; margin-bottom: 20px;">
            <h2 style="color: #4f46e5; margin: 0;">Multitrack</h2>
            <p style="color: #64748b; font-size: 14px; margin-top: 4px;">Goal & Skill Habit Tracker</p>
          </div>
          
          <h3 style="color: #0f172a;">Welcome to Multitrack! 🎉</h3>
          <p style="color: #334155; line-height: 1.6;">
            Thank you for registering. Please click the button below to verify your email address and activate your account.
          </p>
          
          <div style="text-align: center; margin: 30px 0;">
            <a href="%s" style="background-color: #4f46e5; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;">
              Verify Email Address
            </a>
          </div>
          
          <p style="color: #64748b; font-size: 13px; line-height: 1.5;">
            Or copy and paste this link into your browser:<br/>
            <a href="%s" style="color: #4f46e5;">%s</a>
          </p>
          
          <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 30px 0 20px 0;" />
          <p style="color: #94a3b8; font-size: 12px; text-align: center;">
            If you did not request this registration, you can safely ignore this email.
          </p>
        </div>
        """.formatted(verifyUrl, verifyUrl, verifyUrl);

    dispatchEmail(toEmail, subject, htmlContent);
  }

  @Async
  public void sendStaleReminderEmail(String toEmail, String displayName, List<Track> staleTracks) {
    log.info("[EMAIL-DEBUG] Starting sendStaleReminderEmail to: {}", toEmail);

    String subject = "⚠️ Reminder: Your learning tracks need attention!";
    
    StringBuilder tracksListHtml = new StringBuilder();
    for (Track track : staleTracks) {
      tracksListHtml.append("""
          <li style="margin-bottom: 8px; color: #0f172a;">
            <strong>%s</strong> (Streak: %d days)
          </li>
          """.formatted(track.getName(), track.getCurrentStreak()));
    }

    String htmlContent = """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
          <div style="text-align: center; margin-bottom: 20px;">
            <h2 style="color: #4f46e5; margin: 0;">Multitrack</h2>
          </div>
          
          <h3 style="color: #0f172a;">Hi %s, keep your streak alive! 🔥</h3>
          <p style="color: #334155; line-height: 1.6;">
            You have inactive tracks that haven't been checked-in recently:
          </p>
          
          <ul style="background-color: #f8fafc; padding: 16px 24px; border-radius: 8px; border: 1px solid #e2e8f0;">
            %s
          </ul>
          
          <div style="text-align: center; margin: 30px 0;">
            <a href="%s" style="background-color: #4f46e5; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;">
              Check-In Now
            </a>
          </div>
        </div>
        """.formatted(displayName, tracksListHtml.toString(), frontendUrl);

    dispatchEmail(toEmail, subject, htmlContent);
  }

  private void dispatchEmail(String toEmail, String subject, String htmlContent) {
    // If BREVO_API_KEY is present or key starts with xkeysib-
    String activeBrevoKey = (brevoApiKey != null && !brevoApiKey.isBlank()) ? brevoApiKey :
        (resendApiKey != null && resendApiKey.startsWith("xkeysib-")) ? resendApiKey : null;

    if (activeBrevoKey != null) {
      sendViaBrevoApi(activeBrevoKey, toEmail, subject, htmlContent);
    } else {
      sendViaResendHttpApi(toEmail, subject, htmlContent);
    }
  }

  private void sendViaBrevoApi(String apiKey, String toEmail, String subject, String htmlContent) {
    try {
      log.info("[EMAIL-DEBUG] Sending email via Brevo HTTPS REST API to: {}", toEmail);

      String senderAddress = (fromEmail != null && fromEmail.contains("@") && !fromEmail.contains("resend.dev")) 
          ? fromEmail 
          : "noreply@multitrack.app";

      Map<String, Object> body = Map.of(
          "sender", Map.of("name", "Multitrack", "email", senderAddress),
          "to", List.of(Map.of("email", toEmail)),
          "subject", subject,
          "htmlContent", htmlContent
      );

      String response = brevoClient.post()
          .uri("/smtp/email")
          .header("api-key", apiKey)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .body(String.class);

      log.info("[EMAIL-DEBUG] SUCCESS! Brevo API Response: {}", response);
    } catch (Exception e) {
      log.error("[EMAIL-DEBUG] ERROR! Exception sending email via Brevo API to {}: {}", toEmail, e.getMessage(), e);
    }
  }

  private void sendViaResendHttpApi(String toEmail, String subject, String htmlContent) {
    if (resendApiKey == null || resendApiKey.isBlank()) {
      log.warn("[EMAIL-DEBUG] Resend API Key is missing. Skipping email sending.");
      return;
    }

    try {
      log.info("[EMAIL-DEBUG] Sending email via Resend HTTPS API (Port 443) to: {}", toEmail);
      
      Map<String, Object> body = Map.of(
          "from", fromEmail,
          "to", List.of(toEmail),
          "subject", subject,
          "html", htmlContent
      );

      String response = resendClient.post()
          .uri("/emails")
          .header("Authorization", "Bearer " + resendApiKey)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .body(String.class);

      log.info("[EMAIL-DEBUG] SUCCESS! Resend API Response: {}", response);
    } catch (Exception e) {
      log.error("[EMAIL-DEBUG] ERROR! Exception sending email via Resend HTTPS API to {}: {}", toEmail, e.getMessage(), e);
    }
  }
}
