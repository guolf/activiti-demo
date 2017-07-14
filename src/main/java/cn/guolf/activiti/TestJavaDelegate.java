package cn.guolf.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import java.util.Map;

/**
 * Created by guolf on 17/7/2.
 */
public class TestJavaDelegate implements JavaDelegate {

    public void execute(DelegateExecution delegateExecution) throws Exception {
        Map var = delegateExecution.getVariables();

        System.out.println("delegateExecution = " + delegateExecution.getEventName());

        //todo 从redis中查询是否有驳回标识，如果有，将confirmPass设置为flase
        delegateExecution.setVariable("confirmPass", true);

        System.out.println("var = " + var);
    }
}
