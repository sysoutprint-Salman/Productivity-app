package kanban;

public class Enums {
    public enum Section {ID, TITLE, DESCRIPTION, COLOR, STATUS, POSITION, PRIORITY, DUE_DATE, INBOX, LIST}
    public enum LS {ACTIVE, ARCHIVED, DELETED} //List status
    public enum CS {ACTIVE, ARCHIVED, PARENT_ARCHIVED, PARENT_DELETED, INBOXED} //Card status

}
