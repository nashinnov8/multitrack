package org.nashinnov8.multitrack.common.service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${MAIL_FROM:onboarding@resend.dev}")
  private String fromEmail;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  @Async
  public void sendVerificationEmail(String toEmail, String token) {
    String verifyUrl = frontendUrl + "/verify-email?token=" + token;
    String subject = "Verify your Multitrack account";
    
    String htmlContent = """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; rounded-radius: 12px; background-color: #ffffff;">
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

    sendHtmlEmail(toEmail, subject, htmlContent);
  }

  @Async
  public void sendStaleReminderEmail(String toEmail, String displayName, List<Track> staleTracks) {
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

    sendHtmlEmail(toEmail, subject, htmlContent);
  }

  private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);
      mailSender.send(mimeMessage);
      log.info("Email sent successfully to {}", toEmail);
    } catch (Exception e) {
      log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
    }
  }
}
