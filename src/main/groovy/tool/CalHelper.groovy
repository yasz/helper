package tool

/**
 * Created by Peter.Yang on 2020/11/21.
 */
class CalHelper {
    static void main(String[] args) {

        for (i in 1..100) {
            println(dse1002va100(i,  [72,67,62,52,46,38,1]))
        }
        println(dse1002va100(62.2))
    }

    static Double dse1002va100(def dseScore, def dseLv) {
        // 2022.1.19 将考试局100分转换为va100

        def standard = [90, 80, 70, 60, 40, 20, 1]
        if (dseLv == null) {
            dseLv = [90, 80, 70, 60, 40, 20, 1]
        }
        if (dseScore >= dseLv[0]) {
            return standard[0] + (100 - standard[0]) * (dseScore - dseLv[0]) / (100 - dseLv[0])
        } else if (dseScore >= dseLv[1]) {
            return standard[1] + (standard[0] - standard[1]) * (dseScore - dseLv[1]) / (dseLv[0] - dseLv[1])
        } else if (dseScore >= dseLv[2]) {
            return standard[2] + (standard[1] - standard[2]) * (dseScore - dseLv[2]) / (dseLv[1] - dseLv[2])
        } else if (dseScore >= dseLv[3]) {
            return standard[3] + (standard[2] - standard[3]) * (dseScore - dseLv[3]) / (dseLv[2] - dseLv[3])
        } else if (dseScore >= dseLv[4]) {
            return standard[4] + (standard[3] - standard[4]) * (dseScore - dseLv[4]) / (dseLv[3] - dseLv[4])
        } else if (dseScore >= dseLv[5]) {
            return standard[5] + (standard[4] - standard[5]) * (dseScore - dseLv[5]) / (dseLv[4] - dseLv[5])
        } else if (dseScore >= dseLv[6]) {
            return standard[6] + (standard[5] - standard[6]) * (dseScore - dseLv[6]) / (dseLv[5] - dseLv[6])
        } else return 0
    }

    static int vascore4(def score100, def lv) {
        if (lv == null) {
            lv = [90, 80, 70, 60, 40, 20, 1]
        }

        if (score100 >= lv[0]) {
            return 7
        } else if (score100 >= lv[1]) {
            return 6
        } else if (score100 >= lv[2]) {
            return 5
        } else if (score100 >= lv[3]) {
            return 4
        } else if (score100 >= lv[4]) {
            return 3
        } else if (score100 >= lv[5]) {
            return 2
        } else if (score100 >= lv[6]) {
            return 1
        } else return 0

    }

    static int vascore2(def score100) {

        if (score100 >= 90) {
            return 7
        } else if (score100 >= 80) {
            return 6
        } else if (score100 >= 70) {
            return 5
        } else if (score100 >= 60) {
            return 4
        } else if (score100 >= 40) {
            return 3
        } else if (score100 >= 20) {
            return 2
        } else if (score100 >= 1) {
            return 1
        } else return 0
    }

    static Double vascore(def score7) {
        Double f
        if (score7.getClass().toString().contains("String")) {
            if (score7 == "") {
                return null
            }
            f = Double.parseDouble(score7)
        } else {
            f = score7
        }

        int roundscore7 = (int) (f * 100) % 100
        if (roundscore7 < 25) {
            f = Math.floor(f)
        } else if (roundscore7 < 75) {
            f = Math.floor(f) + 0.5
        } else {
            f = Math.floor(f) + 1
        }
        return f
    }
}
