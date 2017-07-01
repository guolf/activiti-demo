package cn.guolf.activiti;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guolf on 17/7/1.
 * 动态设置参与公示确认人员
 */
public class MyTaksListener implements TaskListener {

    public void notify(DelegateTask delegateTask) {
        System.out.println("delegateTask.getEventName() = " + delegateTask.getEventName());
        List<String> assigneeList = new ArrayList<String>(); //分配任务的人员
        assigneeList.add("张三");
        assigneeList.add("李四");
        assigneeList.add("王五");
        delegateTask.setVariable("publicityList",assigneeList);
    }
}
