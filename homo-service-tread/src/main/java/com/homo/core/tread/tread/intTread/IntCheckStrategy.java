package com.homo.core.tread.tread.intTread;

import java.util.function.BiPredicate;

public enum IntCheckStrategy implements BiPredicate<Integer, Integer> {
    Equal {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue.equals(checkValue);
        }
    },
    Equal_Zero {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue == 0;
        }
    },
    Greater_ZERO {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue > 0;
        }
    },
    Less_ZERO {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue < 0;
        }
    },
    Greater_Equal_ZERO {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue >= 0;
        }
    },
    Less_Equal_ZERO {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue <= 0;
        }
    },
    Greater {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue > checkValue;
        }
    },
    Less {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue < checkValue;
        }
    },
    Greater_Equal {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue >= checkValue;
        }
    },
    Less_Equal {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return opValue <= checkValue;
        }
    },
    ALWAYS_TRUE {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return true;
        }
    },
    ALWAYS_FALSE {
        @Override
        public boolean test(Integer opValue, Integer checkValue) {
            return false;
        }
    }

}
