package cn.guolf.activiti;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收文流程demo
 *
 * @author guolf
 * @date 2018-01-31
 */
public class DocReceiveDemo {

    public static void main(String[] args) {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000").setJdbcUsername("sa").setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setJobExecutorActivate(true)
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        ProcessEngine processEngine = cfg.buildProcessEngine();
        String pName = processEngine.getName();
        String ver = ProcessEngine.VERSION;
        System.out.println("ProcessEngine [" + pName + "] Version: [" + ver + "]");

        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 流程部署
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("docReceive.bpmn")
                .name("收文流程测试")
                .category("")
                .deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        System.out.println("流程名称 ： [" + processDefinition.getName() + "]， 流程ID ： ["
                + processDefinition.getId() + "], 流程KEY : " + processDefinition.getKey());

        // 设置流程创建人
        IdentityService identityService = processEngine.getIdentityService();
        identityService.setAuthenticatedUserId("createUserId");

        // 收文登记，启动流程
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map vars = new HashMap();
        vars.put("officeLeader", "officeLeader1");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("docReceive", "Key001", vars);

        System.out.println("流程实例ID = " + processInstance.getId());
        System.out.println("正在活动的流程节点ID = " + processInstance.getActivityId());
        System.out.println("流程定义ID = " + processInstance.getProcessDefinitionId());

        // 办公室负责人待办任务查询
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList1 = taskService.createTaskQuery().taskAssignee("officeLeader1").orderByTaskCreateTime().desc().list();
        System.out.println("taskList1 = " + taskList1);

        // 办公室负责人审核

        vars.clear();
        vars.put("pass", "yes");
        // 并行
        // vars.put("type","parallel");
        // 串行
        vars.put("type", "serial");
        // 设置分管领导
        List<String> assigneeList = new ArrayList<String>();
        assigneeList.add("leader1");
        assigneeList.add("leader2");
        assigneeList.add("leader3");
        vars.put("leaderList", assigneeList);

        Task task1 = taskList1.get(0);
        taskService.complete(task1.getId(), vars);

        // 分管领导1审核
        List<Task> taskList2 = taskService.createTaskQuery().taskAssignee("leader1").orderByTaskCreateTime().desc().list();
        System.out.println("taskList2 = " + taskList2);
        taskService.complete(taskList2.get(0).getId());

        // 分管领导2审核
        List<Task> taskList3 = taskService.createTaskQuery().taskAssignee("leader2").orderByTaskCreateTime().desc().list();
        System.out.println("taskList3 = " + taskList3);
        taskService.complete(taskList3.get(0).getId());

        // 分管领导3审核
        List<Task> taskList4 = taskService.createTaskQuery().taskAssignee("leader3").orderByTaskCreateTime().desc().list();
        System.out.println("taskList4 = " + taskList4);
        taskService.complete(taskList4.get(0).getId());

        // 办公室工作人员分发
        List<Task> taskList5 = taskService.createTaskQuery().taskAssignee("createUserId").orderByTaskCreateTime().desc().list();
        System.out.println("taskList5 = " + taskList5);

        vars.clear();
        // 设置承办科室
        List<String> deptList = new ArrayList<String>();
        deptList.add("dept1");
        deptList.add("dept2");
        vars.put("deptList", deptList);
        taskService.complete(taskList5.get(0).getId(), vars);

        // 承办科室1受理
        List<Task> taskList6 = taskService.createTaskQuery().taskAssignee("dept1").orderByTaskCreateTime().desc().list();
        System.out.println("taskList6 = " + taskList6);
        vars.clear();
        List<String> userList1 = new ArrayList<String>();
        userList1.add("dept1-user1");
        userList1.add("dept1-user2");
        vars.put("userList", userList1);
        taskService.complete(taskList6.get(0).getId(), vars);

        // 承办科室2受理
        List<Task> taskList7 = taskService.createTaskQuery().taskAssignee("dept2").orderByTaskCreateTime().desc().list();
        System.out.println("taskList7 = " + taskList7);
        vars.clear();
        List<String> userList2 = new ArrayList<String>();
        userList2.add("dept2-user1");
        userList2.add("dept2-user2");
        vars.put("userList", userList2);
        taskService.complete(taskList7.get(0).getId(), vars);

        // 各承办处室承办人承办
        for (String s : userList1) {
            List<Task> taskList8 = taskService.createTaskQuery().taskAssignee(s).orderByTaskCreateTime().desc().list();
            taskService.complete(taskList8.get(0).getId());
        }
        for (String s : userList2) {
            List<Task> taskList8 = taskService.createTaskQuery().taskAssignee(s).orderByTaskCreateTime().desc().list();
            taskService.complete(taskList8.get(0).getId());
        }

        // 历史任务查询
        List<HistoricActivityInstance> historicActivityInstances = processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            System.out.println("任务ID:" + historicActivityInstance.getId());
            System.out.println("流程实例ID:" + historicActivityInstance.getProcessInstanceId());
            System.out.println("活动名称：" + historicActivityInstance.getActivityName());
            System.out.println("办理人：" + historicActivityInstance.getAssignee());
            System.out.println("开始时间：" + historicActivityInstance.getStartTime());
            System.out.println("结束时间：" + historicActivityInstance.getEndTime());
            System.out.println("===========================");
        }
    }
}
