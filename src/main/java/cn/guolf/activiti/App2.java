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
 * Activit工作流demo
 * 包含功能:多实例会签、子流程并行审批、动态设置下一节点执行人员、任务超时自动完成
 */
public class App2 {
    public static void main(String[] args) throws InterruptedException {
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
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("MultiTask2.bpmn")
                .name("流程测试")
                .category("")
                .deploy();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult();
        System.out.println("流程名称 ： [" + processDefinition.getName() + "]， 流程ID ： ["
                + processDefinition.getId() + "], 流程KEY : " + processDefinition.getKey());

        // 启动流程
        RuntimeService runtimeService = processEngine.getRuntimeService();
        List<String> assigneeList = new ArrayList<String>(); //分配任务的人员
        assigneeList.add("tom");
        assigneeList.add("jeck");
        assigneeList.add("mary");
        Map<String, Object> vars = new HashMap<String, Object>(); //参数
        vars.put("assigneeList", assigneeList);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess", vars);

        System.out.println("流程实例ID = " + processInstance.getId());
        System.out.println("正在活动的流程节点ID = " + processInstance.getActivityId());
        System.out.println("流程定义ID = " + processInstance.getProcessDefinitionId());

        // 查询指定人的任务
        // ============ 会签任务开始 ===========
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList1 = taskService.createTaskQuery().taskAssignee("mary").orderByTaskCreateTime().desc().list();
        System.out.println("taskList1 = " + taskList1);

        List<Task> taskList2 = taskService.createTaskQuery().taskAssignee("jeck").orderByTaskCreateTime().desc().list();
        System.out.println("taskList2 = " + taskList2);

        List<Task> taskList3 = taskService.createTaskQuery().taskAssignee("tom").orderByTaskCreateTime().desc().list();
        System.out.println("taskList3 = " + taskList3);

        Map mapConfirm = new HashMap();
        mapConfirm.put("confirmPass",true);

        Task task1 = taskList1.get(0);
        taskService.complete(task1.getId(),mapConfirm);

        Map mapConfirm1 = new HashMap();
        mapConfirm1.put("confirmPass",true);
        Task task2 = taskList2.get(0);
        taskService.complete(task2.getId(),mapConfirm1);

        Task task3 = taskList3.get(0);
        taskService.complete(task3.getId(),mapConfirm);
        // ============ 会签任务结束 ===========

        // 部门主任
        List<Task> taskListDept1 = taskService.createTaskQuery().taskAssignee("dept").orderByTaskCreateTime().desc().list();
        Map<String,Object> mapDept = new HashMap<String, Object>();
        mapDept.put("deptPass",true);
        System.out.println("taskListDept1 = " + taskListDept1);

        taskService.complete(taskListDept1.get(0).getId(),mapDept);

        // =============子流程任务开始==========

        // 市场专员
        List<Task> taskListSczy = taskService.createTaskQuery().taskAssignee("sczy").orderByTaskCreateTime().desc().list();
        Map<String,Object> mapMarket = new HashMap<String, Object>();
        mapMarket.put("marketPass",false);
        System.out.println("taskListSczy = " + taskListSczy);

        taskService.complete(taskListSczy.get(0).getId(),mapMarket);

        // 财务专员
        List<Task> taskListCwzy = taskService.createTaskQuery().taskAssignee("cwzy").orderByTaskCreateTime().desc().list();
        System.out.println("taskListCwzy = " + taskListCwzy);
        Map<String,Object> mapFinance = new HashMap<String, Object>();
        mapFinance.put("financePass",false);
        taskService.complete(taskListCwzy.get(0).getId(),mapFinance);

        // 市场主任
        List<Task> taskListSczr = taskService.createTaskQuery().taskAssignee("me").orderByTaskCreateTime().desc().list();
        System.out.println("taskListSczr = " + taskListSczr);
//        Map<String,Object> mapMarketLeader = new HashMap<String, Object>();
//        mapMarketLeader.put("marketLeaderPass",true);
//        taskService.complete(taskListSczr.get(0).getId());

        // 财务主任
        List<Task> taskListCwzr = taskService.createTaskQuery().taskAssignee("cwzr").orderByTaskCreateTime().desc().list();
        System.out.println("taskListCwzr = " + taskListCwzr);

//        taskService.complete(taskListCwzr.get(0).getId());
        // =============子流程任务结束==========

        // 技术专员审批
        List<Task> taskListJishu = taskService.createTaskQuery().taskAssignee("jishu").orderByTaskCreateTime().desc().list();
        System.out.println("taskListJishu = " + taskListJishu);
//        taskService.complete(taskListJishu.get(0).getId());
//
//        // 综合部公示
//        List<Task> taskListZonghe = taskService.createTaskQuery().taskAssignee("zhb").orderByTaskCreateTime().desc().list();
//        System.out.println("taskListZonghe = " + taskListZonghe);
//        taskService.complete(taskListZonghe.get(0).getId());
//
//        // 公示确认，5秒未完成自动进入下一流程
//        List<Task> taskListPublic = taskService.createTaskQuery().taskAssignee("张三").orderByTaskCreateTime().desc().list();
//        System.out.println("taskListPublic = " + taskListPublic);
//        taskService.complete(taskListPublic.get(0).getId());
//
//        Thread.sleep(1000*10);
//
//        // 分管领导确认
//        List<Task> taskListLeader = taskService.createTaskQuery().taskAssignee("leader").orderByTaskCreateTime().desc().list();
//        System.out.println("taskListLeader = " + taskListLeader);
//        taskService.complete(taskListLeader.get(0).getId());

        // ==================流程结束======================

        // 历史任务查询
        List<HistoricActivityInstance> historicActivityInstances = processEngine.getHistoryService() // 历史任务Service
                .createHistoricActivityInstanceQuery() // 创建历史活动实例查询
                .processInstanceId("5") // 指定流程实例id
                .finished() // 查询已经完成的任务
                .orderByHistoricActivityInstanceEndTime()
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
