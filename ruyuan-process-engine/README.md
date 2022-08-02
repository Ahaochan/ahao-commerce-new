## 儒猿流程引擎

### 基本概念
流程引擎本质上是一个基于责任链模式拓展的组件，在责任链基础上增加了一些功能。 

在流程引擎中，一个流程由多个流程节点组成，流程节点之间的顺序关系可以通过配置文件来定义，支持动态配置流程节点顺序。
通过流程引擎，可以将系统一些业务逻辑拆分为一个一个节点，然后可以对这些节点进行自由组合，动态切换节点顺序，可以快速适应多变的业务发展。

目前定义了3种类型的流程节点，分别如下：

- 标准流程节点：一个普通的Action
- 可回滚流程节点：当后续节点出现异常的时候，会执行可回滚操作的Action
- 动态流程节点：在程序运行的时候根据状态动态选择下一个节点的Action
 
### 快速启动

Maven引用

```
<dependency>
    <groupId>com.ruyuan</groupId>
    <artifactId>process-engine-wrapper</artifactId>
    <version>0.0.7</version>
</dependency>
```
#### xml方式配置
声明几个节点

标准节点集成StandardProcessor
```
@Component
public class StandardProcessorDemo2 extends StandardProcessor {
    @Override
    protected void processInternal(ProcessContext context) {
        System.out.println("StandProcessor " + context.get("id"));
    }
}
```
可回滚节点继承RollbackProcessor
```
@Component
public class RollBackProcessorDemo extends RollbackProcessor {

    @Override
    protected void processInternal(ProcessContext context) {
        System.out.println("RollBackProcessor " + context.get("id"));
    }

    @Override
    protected void rollback(ProcessContext context) {
        System.out.println("rollback RollBackProcessor " + context.get("id"));
    }
}
```
动态节点继承DynamicProcessor

```
@Component
public class DynamicProcessorDemo extends DynamicProcessor {

    @Override
    protected void processInternal(ProcessContext context) {
        System.out.println("DynamicProcess " + context.get("id"));
    }

    @Override
    protected String nextNodeId(ProcessContext context) {
        return "node4";
    }
}
```
声明xml配置文件：
```
<?xml version="1.0" encoding="UTF-8"?>
<process-context xmlns="http://www.w3school.com.cn"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.w3school.com.cn process-engine.xsd">
    <process name="process1">
    <node name="node1" class="xxx.StandardProcessorDemo" next="node2" begin="true"/>
    <node name="node2" class="xxx.RollBackProcessorDemo" next="node3"/>
    <node name="node3" class="xxx.DynamicProcessorDemo" next="node4,node5"/>
    <node name="node4" class="xxx.StandardProcessorDemo"/>
    <node name="node5" class="xxx.StandardProcessorDemo" invoke-method="sync"/>
    </process>
</process-context>
```
其中配置文件的意思是，声明了一个process1的流程，其中第一个节点为node1,下一个节点为node2，再下一个节点为node3，
node3的下一个节点可能是node4或者node5, 这要根据代码运行判断。上面的代码例子中，node3的下一个节点指定了为id是node4的节点。

运行代码：
```
public static void main(String[] args) throws Exception {
    ProcessEngine processEngine = new ProcessEngine(
    new ClassPathXmlProcessParser("process-demo.xml"));
    ProcessContext process1 = processEngine.getContext("process1");
    process1.set("nextId", "node4");
    process1.start();
}
```

其中可以通过ProcessContext.set(key,value)方法在不同的节点中传递信息。

#### 注解方式集成
在启动类加上@EnableProcessEngine("packageName"),
value值为扫描节点的包名，接着在每一个节点上面加上注解即可。框架会自动扫描节点，并根据groupId 聚合节点构建成一个流程
```
@Slf4j
@RestController
@SpringBootApplication
@EnableProcessEngine("process-demo.xml")
public class Application {
    @Autowired
    private ProcessContextFactory processContextFactory;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/test1")
    public String test1() {
        ProcessContext process1 = processContextFactory.getContext("process1");
        process1.set("id", "process1");
        process1.start();
        return "true";
    }
}
```

### 动态刷新流程节点配置
流程引擎的一大优势是可以任意调整流程的顺序，可以配合配置中心实现动态更新流程配置。

以下例子以nacos作为配置中心为例：

nacos上面添加配置：

![](img/pic1.png)

接着在应用程序启动的时候加入如下代码，即可重新刷新流程

```
String serverAddr = "localhost";
String dataId = "xml.demo";
String group = "DEFAULT_GROUP";
Properties properties = new Properties();
properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
ConfigService configService = NacosFactory.createConfigService(properties);
String content = configService.getConfig(dataId, group, 5000);
configService.addListener(dataId, group, new AbstractListener() {
    @Override
    public void receiveConfigInfo(String configInfo) {
        StringXmlProcessParser stringXmlProcessParser = new
        StringXmlProcessParser(configInfo);
        processContextFactory.refresh(stringXmlProcessParser.parse());
        System.out.println("recieve:" + configInfo);
    }
});
```
