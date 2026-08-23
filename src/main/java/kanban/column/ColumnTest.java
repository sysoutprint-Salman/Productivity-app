package kanban.column;

import JavaFX.Enums;
import org.junit.Test;

import java.util.List;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class ColumnTest {
    private final ColumnService columnService = new ColumnService();
    private final Long boardId = 1L; // Board belonging to user_id = 2


    private Column createTestColumn(String title, Long position) {
        Column column = new Column();
        column.setBoardId(boardId);
        column.setColumnPosition(position);
        column.setTitle(title);
        column.setHexColor("#FFFFFF");
        column.setStatus(Enums.LS.ACTIVE);

        columnService.create(column);
        return column;
    }

    @Test
    public void createAndFindColumn() {
        Column created = createTestColumn("Test Column", 0L);

        Column found = columnService.findColumnById(created.getColumnId());

        assertNotNull(found);
        assertEquals(created.getColumnId(), found.getColumnId());
        assertEquals(found.getTitle(), "Test Column");
        assertEquals(found.getColumnPosition(), 0L);

        columnService.deleteColumn(created.getColumnId());
    }

    @Test
    public void findAllColumnsByBoardId() {
        Column created = createTestColumn("Test Column", 0L);

        List<Column> columns =
                columnService.findAllColumnsByBoardId(boardId);

        assertTrue(
                columns.stream()
                        .anyMatch(c -> c.getColumnId()
                                .equals(created.getColumnId()))
        );

        columnService.deleteColumn(created.getColumnId());
    }

    @Test
    public void findByStatus() {
        Column created = createTestColumn("Test Column", 0L);

        List<Column> columns =
                columnService.findByStatus(boardId, Enums.LS.ACTIVE);

        assertTrue(
                columns.stream()
                        .anyMatch(c -> c.getColumnId()
                                .equals(created.getColumnId()))
        );

        columnService.deleteColumn(created.getColumnId());
    }

    @Test
    public void updateColumn() {
        Column column = createTestColumn("Original", 0L);

        Column updated = new Column();
        updated.setBoardId(boardId);
        updated.setColumnPosition(5L);
        updated.setTitle("Updated");
        updated.setHexColor("#000000");
        updated.setStatus(Enums.LS.DELETED);

        columnService.updateColumn(column.getColumnId(), updated);

        Column result =
                columnService.findColumnById(column.getColumnId());

        assertEquals("Updated", result.getTitle());
        assertEquals(5L, result.getColumnPosition());
        assertEquals("#000000", result.getHexColor());
        assertEquals(Enums.LS.DELETED, result.getStatus());

        columnService.deleteColumn(column.getColumnId());
    }

    @Test
    public void updateColumnSection() {
        Column column = createTestColumn("Original", 0L);

        columnService.updateColumnSection(
                column.getColumnId(),
                Enums.Section.TITLE,
                "Updated"
        );

        Column result =
                columnService.findColumnById(column.getColumnId());

        assertEquals("Updated", result.getTitle());

        columnService.deleteColumn(column.getColumnId());
    }

    @Test
    public void updateColumnSectionRejectsNull() {
        Column column = createTestColumn("Test", 0L);

        assertThrows(
                NullPointerException.class,
                () -> columnService.updateColumnSection(
                        column.getColumnId(),
                        Enums.Section.TITLE,
                        null
                )
        );

        columnService.deleteColumn(column.getColumnId());
    }

    @Test
    public void updateStatusAndPosition() {
        Column column = createTestColumn("Test", 0L);

        columnService.updateStatusAndPosition(
                column.getColumnId(),
                Enums.LS.DELETED,
                3L
        );

        Column result =
                columnService.findColumnById(column.getColumnId());

        assertEquals(Enums.LS.DELETED, result.getStatus());
        assertEquals(3L, result.getColumnPosition());

        columnService.deleteColumn(column.getColumnId());
    }

    @Test
    public void updateStatusAndPositionCanClearPosition() {
        Column column = createTestColumn("Test", 0L);

        columnService.updateStatusAndPosition(
                column.getColumnId(),
                Enums.LS.DELETED,
                null
        );

        Column result =
                columnService.findColumnById(column.getColumnId());

        assertEquals(Enums.LS.DELETED, result.getStatus());
        assertNull(result.getColumnPosition());

        columnService.deleteColumn(column.getColumnId());
    }

    @Test
    public void updateColumnPositions() {
        Column first = createTestColumn("First", 0L);
        Column second = createTestColumn("Second", 1L);

            /*columnService.updateColumnPositions(
                    List.of(first.getColumnId(), second.getColumnId()),
                    List.of(1L, 0L)
            );*/

        assertEquals(
                1L,
                columnService.findColumnById(first.getColumnId())
                        .getColumnPosition()
        );

        assertEquals(
                0L,
                columnService.findColumnById(second.getColumnId())
                        .getColumnPosition()
        );

        columnService.deleteColumn(first.getColumnId());
        columnService.deleteColumn(second.getColumnId());
    }

        /*@Test
        public void updateColumnPositionsRejectsMismatchedLists() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> columnService.updateColumnPositions(
                            List.of(1L, 2L),
                            List.of(0L)
                    )
            );
        }*/

    @Test
    public void deleteColumn() {
        Column column = createTestColumn("Delete Me", 0L);

        columnService.deleteColumn(column.getColumnId());

        assertNull(
                columnService.findColumnById(column.getColumnId())
        );
    }

}
