package org.hao.core.print;


import org.springframework.util.ObjectUtils;

public enum PrintUtil {
    RED {
        public Integer getColor() {
            return 31;
        }
    },
    GREEN {
        public Integer getColor() {
            return 32;
        }
    },
    YELLOW {
        public Integer getColor() {
            return 33;
        }
    },
    BLUE {
        public Integer getColor() {
            return 34;
        }
    },
    PURPULE {
        public Integer getColor() {
            return 35;
        }
    },
    CYAN {
        public Integer getColor() {
            return 36;
        }
    },
    WHITE {
        public Integer getColor() {
            return 37;
        }
    },
    BLACK {
        public Integer getColor() {
            return 30;
        }
    };

    public Integer getColor() {
        throw new AbstractMethodError();
    }

    public void Println(Object val) {
        val = ObjectUtils.isEmpty(val) ? "null" : val;
        printSingleColor(getColor(), 2, val.toString());
    }

    public void Println(Object val, PrintUtil background) {
        val = ObjectUtils.isEmpty(val) ? "null" : val;
        printSingleColor(getColor(), background.getColor()+10,2, val.toString());
    }

    /**
     * @param code    颜色代号：背景颜色代号(41-46)；前景色代号(31-36)
     * @param n       数字+m：1加粗；3斜体；4下划线
     * @param content 要打印的内容
     *                格式：System.out.println("\33[前景色代号;背景色代号;数字m")
     *                %s是字符串占位符，%d 是数字占位符
     */
    private void printSingleColor(int code, int n, String content) {
        System.out.format("\33[%d;%dm%s\n", code, n, content + "\33[0;39m");
    }

    private void printSingleColor(int code, int backCode, int n, String content) {
        System.out.format("\33[%d;%d;%dm%s\n", code, backCode, n, content + "\33[0;39m");
    }
}
