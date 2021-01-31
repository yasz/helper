package tool

/**
 * Created by Peter.Yang on 2020/11/21.
 */
class CalHelper {
    static int vascore3(def score100,def flag,def flag2) {

        def lv= [90,80,70,60,40,20,1]
        if(flag == '中文' && flag2=='10v'){
            lv=  [71,66,60,50,40,30,20]
        }else if(flag == '中文' && flag2=='11v'){
            lv=  [72,67,63,57,51,40,1]

        }

        if (score100>=lv[0]){return 7}
        else if(score100>=lv[1]){return 6}
        else if(score100>=lv[2]){return 5}
        else if(score100>=lv[3]){return 4}
        else if(score100>=lv[4]){return 3}
        else if(score100>=lv[5]){return 2}
        else if(score100>=lv[6]){return 1}
        else return 0
    }

    static int vascore2(def score100) {
        if (score100>=90){return 7}
        else if(score100>=80){return 6}
        else if(score100>=70){return 5}
        else if(score100>=60){return 4}
        else if(score100>=40){return 3}
        else if(score100>=20){return 2}
        else if(score100>=1){return 1}
        else return 0
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
