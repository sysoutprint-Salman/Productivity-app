package SpringBoot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class DTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListReorder{
        private List<Long> listIds = new ArrayList<>();
        private List<Long> listPositions = new ArrayList<>();
    }
}

