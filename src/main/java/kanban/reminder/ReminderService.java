package kanban.reminder;

import JavaFX.Enums;

import java.util.List;
import java.util.Map;

public class ReminderService {

    private final ReminderRepo reminderRepo;

    public ReminderService() {
        reminderRepo = new ReminderRepo();
    }

    public List<Reminder> findByBoardId(Long boardId) {
        return reminderRepo.findByBoardId(boardId);
    }

    public void create(Reminder reminder) {
        reminderRepo.create(reminder);
    }

    public void delete(Long reminderId) {
        reminderRepo.delete(reminderId);
    }

    public void updateReminderSection(Long reminderId, Object value, Enums.Section section) {
        reminderRepo.updateReminderSection(reminderId, value, section);
    }

    public void updateReminderSection(Long reminderId, Enums.Section section, Reminder reminder) {
        reminderRepo.updateReminderSection(reminderId, section, reminder);
    }

    public void updateReminderSections(Long reminderId, Map<Enums.Section, Object> updates) {
        reminderRepo.updateReminderSections(reminderId, updates);
    }

    public void updateFull(Long reminderId, Reminder reminder) {
        reminderRepo.updateFull(reminderId, reminder);
    }
}