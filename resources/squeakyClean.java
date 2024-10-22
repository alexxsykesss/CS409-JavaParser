package goodCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class squeakyClean {

    private class localClass{
        private int aVar;
        public localClass(int i){
            aVar = i;
        }

        private int getVar(){
            return aVar;
        }
    }

    // Acceptable public instance variable
    public class roomNumber{
        String building;
        int room;
        public roomNumber(String building, int room){
            this.building = building;
            this.room = room;
        }
    }

    private static final int TEN = 10;
    private static final int TON = 100;
    private static final char BANG = '!';
    private static final String TRELLIS = "Trellis";

    private int fldi1;
    private localClass fldc1;
    private double fldd;
    private String fls1;

    // Accessors
    public int getFldi1(){
        return fldi1;
    }

    public double getFldd(){
        return fldd;
    }

    public String getFls1(){
        return fls1;
    }

    // Mutators
    public void setFldi1(int val){
        fldi1 =val;
    }

    public void setFldd(double val){
        fldd = val;
    }

    public void setFls1(String val){
        fls1 = val;
    }


    public void noDeclarationOrAssignmentErrors() {
        char c = 'c';
        float f = 0.12345f;
        localClass lc = new localClass(101);
        roomNumber rn = new roomNumber("LT", 1105);
        String whereabouts = rn.building+rn.room;

        char a = 'a';
        char b = 'b';

        int x = 1;
        int y = 2;
        int z = 3;

        z = 123;
        y = z;
        c = 'X';
        b = c;
        a = b;

        fldd = Math.PI;

        if (a == b) {
            char d = c;
            if (x < y) {
                int xx = z;
                double dx = fldd;
                while (x > d) {
                    // duplicate local declaration
                    dx = fldd * fldd;
                }
            }
        }
    }

    public int noSwitchStatementErrors2(String param){
        int result = 0;
        switch(param) {
            case "one":
                result = 1;
                // fall through
            case "two":
                result = result + 1;
                result = 2;
                // fall through
            case "three":
                result = result + 1;
                result = 3;
                break;
            default:
                result = 99;
            }
        return result;
    }

    protected enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
        THURSDAY, FRIDAY, SATURDAY
    }
    protected void noSwitchStatementErrors2(Day day){
        switch (day) {
            case MONDAY:
                System.out.println("Blue");
                break;
            case TUESDAY:
            case WEDNESDAY:
                System.out.println("Grey");
                break;
            case THURSDAY:
                System.out.println("I don't care about you");
                break;
            case FRIDAY:
                System.out.println("I'm in love");
                break;
            case SATURDAY:
                System.out.println("Wait");
                break;
            case SUNDAY:
                System.out.println("Always comes too late");
        }
    }


    public void noConstantUsageErrors(int a, char b, double c, String d){
        while(a != TEN && c > Math.PI ){
            if (b == BANG){
                c = c-0.01; // this is okay
            } else if (d.equals(TRELLIS)) {
                c = c-0.05; // this is okay
            }
            a++;
        }
    }

    public FileInputStream noExceptionHandlingErrors1(FileInputStream fis){
        try{
            File file = new File("a/file/path/location");
            fis = new FileInputStream(file);
        }catch(FileNotFoundException e){
            // I've been expecting you...
        }
        return fis;
    }

    public FileInputStream noExceptionHandlingErrors2(FileInputStream fis){
        try{
            File file = new File("a/file/path/location");
            fis = new FileInputStream(file);
        }catch(FileNotFoundException expected){
        }
        return fis;
    }

    public void noForLoopOrConstantErrors(){
        double trouble = 0.0;
        for(int i = 0, j = 1; i < TON; i++, --j){
            for(int k = -1; k > -j; ++k){
                trouble += 0.7; // this is okay
            }
        }
    }

    // no exposure of private data
    private int veryPrivateData;

    public squeakyClean() {
        veryPrivateData = 42;
    }

    private class accessPoint{
        public int exposePrivateData(){
            return veryPrivateData;
        }
    }
}
