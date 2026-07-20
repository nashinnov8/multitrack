package org.nashinnov8.multitrack.tracking.service;

import org.nashinnov8.multitrack.common.exception.ResourceNotFoundException;
import org.nashinnov8.multitrack.tracking.domain.Track;
import org.nashinnov8.multitrack.tracking.repository.ActivityLogRepository;
import org.nashinnov8.multitrack.tracking.repository.TrackRepository;
import org.nashinnov8.multitrack.user.domain.User;
import org.nashinnov8.multitrack.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import org.nashinnov8.multitrack.tracking.dto.request.ActivityLogRequest;
import org.nashinnov8.multitrack.tracking.dto.response.ActivityLogResponse;
import org.nashinnov8.multitrack.tracking.dto.request.TrackCreateRequest;
import org.nashinnov8.multitrack.tracking.dto.response.TrackResponse;

@Service
public class TrackService {

    private final TrackRepository trackRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    // Dependency Injection thông qua Constructor
    public TrackService(TrackRepository trackRepository, ActivityLogRepository activityLogRepository,
            UserRepository userRepository) {
        this.trackRepository = trackRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TrackResponse createTrack(TrackCreateRequest request) {
        User existingUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));
                
        Track newTrack = Track.builder()
                .name(request.name())
                .description(request.description())
                .user(existingUser)
                .isPublic(request.isPublic())
                .build();

        Track savedTrack = trackRepository.save(newTrack);
        
        return TrackResponse.from(savedTrack);
    }

    public TrackResponse getTrackById(UUID trackId) {
        // TODO 1: Gọi trackRepository.findById(trackId).
        // TODO 2: Nếu Optional rỗng (không tìm thấy), hãy throw ra ResourceNotFoundException.
        // TODO 3: Map đối tượng Track tìm được sang TrackResponse và trả về.
        return null;
    }

    public List<TrackResponse> getAllTracksForUser(UUID userId) {
        // TODO 1: Tìm tất cả Track của user (ví dụ: trackRepository.findByUserId(userId)).
        // TODO 2: Lặp qua danh sách (hoặc dùng stream().map()) để map Track sang TrackResponse và trả về danh sách.
        return null;
    }

    @Transactional
    public ActivityLogResponse logActivity(UUID trackId, ActivityLogRequest request) {
        // TODO 1: Lấy Track ra từ DB thông qua trackRepository.findById(trackId) (chứ không gọi hàm getTrackById ở trên vì nó trả về Response).
        // TODO 2: Tạo một ActivityLog mới, set nội dung bằng request.note() và set Track tương ứng.
        // TODO 3: Lưu ActivityLog thông qua activityLogRepository.save().
        // TODO 4: Cập nhật biến lastActivityAt của Track thành thời điểm hiện tại (Instant.now()).
        // TODO 5: (Nâng cao) Tính toán lại currentStreak (chuỗi ngày liên tục). 
        //         So sánh lastActivityAt cũ với hôm nay để xem có bị đứt chuỗi không.
        // TODO 6: (Nâng cao) Cập nhật longestStreak nếu currentStreak mới lớn hơn.
        // TODO 7: Lưu lại Track đã được cập nhật thông qua trackRepository.save().
        // TODO 8: Map ActivityLog vừa lưu sang ActivityLogResponse và trả về.
        return null;
    }

    public List<TrackResponse> findStaleTracks() {
        // TODO 1: Dùng hàm này cho việc check ngầm mỗi ngày (Scheduler).
        // TODO 2: Lấy ra các Track có trạng thái ACTIVE nhưng lastActivityAt 
        //         đã vượt quá số ngày inactivityThresholdDays.
        // TODO 3: Map danh sách Track tìm được sang List<TrackResponse> và trả về.
        return null;
    }
}
