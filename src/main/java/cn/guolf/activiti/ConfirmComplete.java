package cn.guolf.activiti;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.Map;

/**
 * Created by guolf on 17/7/2.
 * 员工确认会签是否已完成
 */
public class ConfirmComplete implements TaskListener {
    public void notify(DelegateTask delegateTask) {
        System.out.println("delegateTask.getEventName() = " + delegateTask.getEventName());
        System.out.println("assignee = " + delegateTask.getAssignee());
        Map<String, Object> var = delegateTask.getVariables();
        if (var.get("confirmSts") != null) {

            Integer isPass = (Integer) var.get("confirmSts");
            if (isPass == 0) {
                System.out.println(" 确认驳回，打回");
                // 保存状态，在redis中保存一个驳回标识
            }
        }
        System.out.println("var = " + var);
    }

}