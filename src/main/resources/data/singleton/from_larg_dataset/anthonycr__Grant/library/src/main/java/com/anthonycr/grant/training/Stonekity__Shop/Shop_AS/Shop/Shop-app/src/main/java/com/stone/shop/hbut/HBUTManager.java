package com.stone.shop.hbut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.stone.shop.base.application.BaseApplication;
import com.stone.shop.base.config.BmobConfig;
import com.stone.shop.hbut.model.Semester;
import com.stone.shop.hbut.model.SingleGrade;
import com.stone.shop.hbut.model.StudentGrade;
import com.stone.shop.hbut.model.StudentSchedule;
import com.stone.shop.base.util.LocalBroadcasts;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * HBUT相关详细管理
 * <p/>
 * Created by stonekity.shi on 2015/4/6.
 */
public class HBUTManager {
    private static final String TAG = "HBUTManager";

    private static final String SP_HBUT_SEMESTER = "com.stone.shop.SP_HBUT_SEMESTER";
    private static final String KEY_SP_HBUT_SEMESTER = "com.stone.shop.KEY_SP_HBUT_SEMESTER";

    private static final String SP_HBUT_GRADE = "com.stone.shop.SP_HBUT_GRADE";
    private static final String KEY_SP_HBUT_GRADE = "com.stone.shop.KEY_SP_HBUT_GRADE";

    private static final String SP_HBUT_SCHDULE = "com.stone.shop.SP_HBUT_SCHDULE";
    private static final String KEY_SP_HBUT_SCHDULE = "com.stone.shop.KEY_SP_HBUT_SCHDULE";


    private static HBUTManager instance = new HBUTManager();
    //当前学期
    private Semester curSemester;

    //所有学期的列表
    private HashMap<String, String> semesterMap = new HashMap<>();

    // 学生成绩
    private StudentGrade sg;

    //学生课表
    private StudentSchedule ss;

    private HBUTManager() {
    }

    public static HBUTManager getInstance() {
        return instance;
    }


    public Semester getCurSemester() {
        if (curSemester == null) {
            return (Semester) find(KEY_SP_HBUT_SEMESTER);
        }
        return curSemester;
    }


    public HashMap<String, String> getAllSemester() {
        return semesterMap;
    }

    public StudentGrade getStudentGrade() {
        if (sg == null) {
            return (StudentGrade) find(KEY_SP_HBUT_GRADE);
        }
        return this.sg;
    }

    public StudentSchedule getStudentSchedule() {
        if (ss == null) {
            return (StudentSchedule) find(KEY_SP_HBUT_SCHDULE);
        }
        return this.ss;
    }


    public void setCurSemester(Semester curSemester) {
        this.curSemester = curSemester;

        if (BmobConfig.DEBUG)
            Log(curSemester);

        //TODO 保存数据
        save(curSemester);
    }


    public void setStudentGrade(StudentGrade sg) {
        if (null == sg)
            return;
        this.sg = sg;

        if (BmobConfig.DEBUG)
            Log(sg);

        //对成绩进行分组排序
        sort(sg.getStuGradeList());
        getAllTaskNo(sg.getStuGradeList());

        //TODO 数据保存
        save(sg);
    }

    public void setStudentSchedule(StudentSchedule ss) {
        if (null == ss)
            return;
        this.ss = ss;

        if (BmobConfig.DEBUG)
            Log(ss);

        //TODO 数据保存
        save(ss);

        //TODO 通知数据数据同步
        LocalBroadcasts.sendLocalBroadcast(new Intent());
    }


    /**
     * 数据本地持久化
     *
     * @param obj
     */
    private void save(Object obj) {
        if (obj == null)
            return;
        Gson gson = new Gson();
        String json = gson.toJson(obj);
        if (obj instanceof Semester) {
            SharedPreferences sp = BaseApplication.getAppContext()
                    .getSharedPreferences(SP_HBUT_SEMESTER, Context.MODE_PRIVATE);
            sp.edit().clear().putString(KEY_SP_HBUT_SEMESTER, json).commit();
        } else if (obj instanceof StudentGrade) {
            SharedPreferences sp = BaseApplication.getAppContext()
                    .getSharedPreferences(SP_HBUT_GRADE, Context.MODE_PRIVATE);
            sp.edit().clear().putString(KEY_SP_HBUT_GRADE, json).commit();
        } else if (obj instanceof StudentSchedule) {
            SharedPreferences sp = BaseApplication.getAppContext()
                    .getSharedPreferences(SP_HBUT_SCHDULE, Context.MODE_PRIVATE);
            sp.edit().clear().putString(KEY_SP_HBUT_SCHDULE, json).commit();
        } else {
            // do nothing
        }
    }


    private Object find(String key) {
        Gson gson = new Gson();
        if (key.equals(KEY_SP_HBUT_SEMESTER)) {
            SharedPreferences sp = BaseApplication.getAppContext().getSharedPreferences(SP_HBUT_SEMESTER, Context.MODE_PRIVATE);
            String value = sp.getString(key, "");
            return gson.fromJson(value, Semester.class);
        } else if (key.equals(KEY_SP_HBUT_GRADE)) {
            SharedPreferences sp = BaseApplication.getAppContext().getSharedPreferences(SP_HBUT_GRADE, Context.MODE_PRIVATE);
            String value = sp.getString(key, "");
            return gson.fromJson(value, StudentGrade.class);
        } else if (key.equals(KEY_SP_HBUT_SCHDULE)) {
            SharedPreferences sp = BaseApplication.getAppContext().getSharedPreferences(KEY_SP_HBUT_SCHDULE, Context.MODE_PRIVATE);
            String value = sp.getString(key, "");
            return gson.fromJson(value, StudentSchedule.class);
        } else {
            // do nothing
        }

        return null;
    }

    //----------------------------------------对成绩进行分组排序---------------------------------------

    /**
     * 排序成绩列表
     *
     * @param list
     */
    private void sort(List<SingleGrade> list) {
        if (list == null || list.size() == 0)
            return;
        Collections.sort(list, comparator);
    }


    /**
     * 获取学期
     *
     * @param list
     */
    private void getAllTaskNo(List<SingleGrade> list) {
        if (null == list || list.size() == 0)
            return;

        Semester curSemester = HBUTManager.getInstance().getCurSemester();
        if (curSemester == null)
            return;

        Iterator<SingleGrade> iterator = list.iterator();
        while (iterator.hasNext()) {
            SingleGrade grade = iterator.next();
            String semester = grade.getTaskNO().substring(0, 5);
            if (!semester.equals(curSemester.getKey()) && !semesterMap.containsKey(semester))
                semesterMap.put(semester, "");
        }

        semesterMap.put(curSemester.getKey(), curSemester.getValue());
    }

    private Comparator<SingleGrade> comparator = new Comparator<SingleGrade>() {
        @Override
        public int compare(SingleGrade lhs, SingleGrade rhs) {
            Long lTaskNo = Long.parseLong(lhs.getTaskNO());
            Long rTaskNo = Long.parseLong(rhs.getTaskNO());
            Long diff = lTaskNo - rTaskNo;
            if (diff > 0)
                return 1;
            else if (diff < 0)
                return -1;
            else
                return 0;
        }
    };

    //----------------------------------------对成绩进行分组排序---------------------------------------


    //-------------------------------------DEBUG--------------------------------------------------
    private void Log(Semester s) {
        if (s == null)
            return;
        Log.d(TAG, String.format("Semester: key = %s, value = %s", s.getKey(), s.getValue()));
    }

    private void Log(StudentSchedule ss) {
        if (ss == null)
            return;
        Log.d(TAG, "StudentSchedule:  title " + ss.getTitle() + " content " + ss.getContent() + " isTeacher " + ss.getIsTeacher() + " schedule count " + ss.getTimeScheduleList().size());
    }

    private void Log(StudentGrade sg) {
        if (sg == null)
            return;
        Log.d(TAG, "StudentGrade:  title " + sg.getTitle() + " grade list count " + sg.getStuGradeList().size());
    }
    //-------------------------------------DEBUG--------------------------------------------------


}
