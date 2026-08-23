package kanban.column;

import JavaFX.Enums;

import java.util.List;

public class ColumnService {

    private final ColumnRepo columnRepo;

    public ColumnService() {
        columnRepo = new ColumnRepo();
    }

    public List<Column> findByStatus(Long boardId, Enums.LS status) {
        return columnRepo.findByStatus(boardId, status);
    }

    public void create(Column column) {
        columnRepo.create(column);
    }

    public void updateColumn(Long columnId, Column newColumnInfo) {
        columnRepo.updateColumn(columnId, newColumnInfo);
    }

    public void updateColumnSection(Long columnId, Enums.Section section, Object value) {
        if (value == null) {
            throw new NullPointerException(
                    "Null value detected, column couldn't be updated."
            );
        }

        columnRepo.updateColumnSection(columnId, section, value);
    }

    public void updateColumnPositions(List<Column> columnsToUpdate) {
        columnRepo.updateColumnPositions(columnsToUpdate);
    }

    public void updateStatusAndPosition(Long columnId, Enums.LS status, Long position) {
        columnRepo.updateStatusAndPosition(columnId, status, position);
    }

    public void deleteColumn(Long columnId) {
        columnRepo.deleteColumn(columnId);
    }

    public List<Column> findAllColumnsByBoardId(Long boardId) {
        return columnRepo.findAllColumnsByBoardId(boardId);
    }

    public Column findColumnById(Long columnId) {
        return columnRepo.findColumnById(columnId);
    }
}
