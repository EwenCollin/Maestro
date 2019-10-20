package net.gamescode.ewen.maestro;

public class Utils {

    public static int frequencyToNoteN(float frequency) {
        if (frequency == -1) return -50;
        int result = (int) Math.round(((Math.log(frequency/440))/Math.log(2))*12);
        return result;
    }

    public static String noteNToNoteSfr(int note) {
        if( note < -24) {
            return "Basse";
        }
        if (note == -24) {
            return "La 1";
        }
        if (note == -23) {
            return "La# 1 / Sib 1";
        }
        if (note == -22) {
            return "Si 1";
        }
        if (note == -21) {
            return "Do 2";
        }
        if (note == -20) {
            return "Do# 2 / Réb 2";
        }
        if (note == -19) {
            return "Ré 2";
        }
        if (note == -18) {
            return "Ré# 2 / Mib 2";
        }
        if (note == -17) {
            return "Mi 2";
        }
        if (note == -16) {
            return "Fa 2";
        }
        if (note == -15) {
            return "Fa# 2 / Solb 2";
        }
        if (note == -14) {
            return "Sol 2";
        }
        if (note == -13) {
            return "Sol# 2 / Lab 2";
        }
        if (note == -12) {
            return "La 2";
        }
        if (note == -11) {
            return "La# 2 / Sib 2";
        }
        if (note == -10) {
            return "Si 2";
        }
        if (note == -9) {
            return "Do 3";
        }
        if (note == -8) {
            return "Do# 3 / Réb 3";
        }
        if (note == -7) {
            return "Ré 3";
        }
        if (note == -6) {
            return "Ré# 3 / Mib 3";
        }
        if (note == -5) {
            return "Mi 3";
        }
        if (note == -4) {
            return "Fa 3";
        }
        if (note == -3) {
            return "Fa# 3 / Solb 3";
        }
        if (note == -2) {
            return "Sol 3";
        }
        if (note == -1) {
            return "Sol# 3 / Lab 3";
        }
        if (note == 0) {
            return "La 3";
        }
        if (note == 1) {
            return "La# 3 / Sib 3";
        }
        if (note == 2) {
            return "Si 3";
        }
        if (note == 3) {
            return "Do 4";
        }
        if (note == 4) {
            return "Do# 4 / Réb 4";
        }
        if (note == 5) {
            return "Ré# 4 / Mib 4";
        }
        if (note == 6) {
            return "Mi 4";
        }
        if (note == 7) {
            return "Fa 4";
        }
        if (note == 8) {
                return "Fa# 4 / Solb 4";
        }
        if (note == 9) {
            return "Sol 4";
        }
        if (note == 10) {
            return "Sol# 4 / Lab 4";
        }
        if (note == 11) {
            return "La 4";
        }
        if (note == 12) {
            return "La# 4 / Sib 4";
        }
        if (note == 13) {
            return "Si 4";
        }
        if (note == 14) {
            return "Do 5";
        }
        if (note == 15) {
            return "Do# 5 / Réb 5";
        }
        if (note == 16) {
            return "Ré 5";
        }
        if (note == 18) {
            return "Ré# 5 / Mib 5";
        }
        if (note == 19) {
            return "Mi 5";
        }
        if (note == 20) {
            return "Fa 5";
        }
        if (note == 21) {
            return "Fa# 5 / Solb 5";
        }
        if (note == 22) {
            return "Sol 5";
        }
        if (note == 23) {
            return "Sol# 5 / Lab 5";
        }
        if (note == 24) {
            return "La 5";
        }
        if (note == 25) {
            return "La# 5 / Sib 5";
        }
        if (note == 26) {
            return "Si 5";
        }
        if (note == 27) {
            return "Do 6";
        }
        if (note == 28) {
            return "Do# 6 / Réb 6";
        }
        if (note == 29) {
            return "Ré 6";
        }
        if (note == 30) {
            return "Ré# 6 / Mib 6";
        }
        if (note == 31) {
            return "Mi 6";
        }
        if (note == 32) {
            return "Fa 6";
        }
        if (note == 33) {
            return "Fa# 6 / Solb 6";
        }
        if (note == 34) {
            return "Sol 6";
        }
        if (note == 35) {
            return "Sol# 6 / Lab 6";
        }
        if (note > 35) {
            return "Haute";
        }
        return "Out of detection";
    }
}
