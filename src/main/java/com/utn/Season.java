package com.utn;

import java.util.Arrays;
import java.util.List;

public enum Season {
    SPRING{
        @Override
        public List<Integer> getMonths() {
            return Arrays.asList(9, 10, 11);
        }
    },
    SUMMER{
        @Override
        public List<Integer> getMonths() {
            return Arrays.asList(1, 2);
        }
    },
    FALL{
        @Override
        public List<Integer> getMonths() {
            return Arrays.asList(3, 4, 5);
        }
    },
    WINTER{
        @Override
        public List<Integer> getMonths() {
            return Arrays.asList(6, 7, 8);
        }
    };

    public abstract List<Integer> getMonths();
}
