package cn.guolf.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.HistoricVariableInstance;

import java.util.List;

/**
 * 多实例完成控制
 * Created by guolf on 17/7/2.
 */
public class TestJavaDelegate implements JavaDelegate {

    public void execute(DelegateExecution delegateExecution) throws Exception {

        List<HistoricVariableInstance> list = delegateExecution.getEngineServices()
                .getHistoryService()
                .createHistoricVariableInstanceQuery()
                .list();
        int pass = 0; // 通过数量
        int refuse = 0; // 驳回数量
        int total = 0; // 总数
        int complete = 0;//完成数
        for (HistoricVariableInstance historicVariableInstance : list) {
            if (historicVariableInstance.getVariableName().equals("confirmSts")) {
                if (historicVariableInstance.getValue().toString().equals("0")) {
                    refuse++;
                } else {
                    pass++;
                }
            } else if (historicVariableInstance.getVariableName().equals("nrOfInstances")) {
                total = Integer.parseInt(historicVariableInstance.getValue().toString());
            } else if (historicVariableInstance.getVariableName().equals("nrOfCompletedInstances")) {
                complete = Integer.parseInt(historicVariableInstance.getValue().toString());
            }
        }

        System.out.println("refuse = " + refuse + ",pass = " + pass + ",total = " + total + ",complete = " + complete);

        if (refuse > 0) {
            // 拒绝人数大于1，驳回
            delegateExecution.setVariable("confirmPass", false);
        } else {
            // 拒绝人数为0，通过
            delegateExecution.setVariable("confirmPass", true);
        }

    }
}
