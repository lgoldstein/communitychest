import java.lang.management.*

import javax.management.*
import javax.management.remote.*

import com.sun.tools.attach.*
import com.sun.tools.attach.spi.*

// SYNOPSIS: groovy -cp path/to/tools.jar localjmx.groovy pid1 pid2 ...  -- PID(s) of JVMs

/*
for (def provider in AttachProvider.providers()) {
    System.out.append(provider.name()).append(": ").println(provider.type())

    for (def vmd in provider.listVirtualMachines()) {
        System.out.append('\t').append(vmd.id()).append(": ").println(vmd.displayName())
    }
}
*/

// see https://blogs.oracle.com/CoreJavaTechTips/entry/the_attach_api
for (def vmid in this.args) {
    def vm = VirtualMachine.attach(vmid)
    def props = vm.agentProperties
    def connectorAddr = props["com.sun.management.jmxremote.localConnectorAddress"]
    if (connectorAddr == null) {
        log("Resolve connector address")
        def sysprops = vm.systemProperties
        def javaHome = sysProps["java.home"]
        def agentPath = javaHome + File.separator + "lib" + File.separator + "management-agent.jar"
        vm.loadAgent(agentPath)
      
        if ((connectorAddr=vm.agentProperties["com.sun.management.jmxremote.localConnectorAddress"]) == null) {
            throw new IllegalStateException("Failed to load agent at: " + agentPath)
        }
    }
    
    // log(connectorAddr)

    def serviceURL = new JMXServiceURL(connectorAddr)
    def connector = JMXConnectorFactory.connect(serviceURL)
    try {
        showJMXData(connector.getMBeanServerConnection())
    } finally {
        connector.close()
    }
}

private static void showJMXData(mbsc) {
    showAllAttributes(mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME)
    showAllAttributes(mbsc, ManagementFactory.MEMORY_MXBEAN_NAME)
    showAllAttributes(mbsc, ManagementFactory.RUNTIME_MXBEAN_NAME)
//    showAllAttributes(mbsc, ManagementFactory.THREAD_MXBEAN_NAME)
//    showAllAttributes(mbsc, ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE)
    showAllAttributes(mbsc, ManagementFactory.MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE)
    showAllAttributes(mbsc, ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE)
}

private static showAllAttributes(mbsc, name) {
    try {
        def     objName=(name instanceof ObjectName) ? name : new ObjectName(name.toString())
        def     mbeanInfo=mbsc.getMBeanInfo(objName)
        List    names=[]
        for (def attr in mbeanInfo.attributes) {
            if (attr.readable) {
                names.add(attr.name)
            }
        }
        
        showAttributes(mbsc, objName, names.toArray(new String[names.size()]))
    } catch(Exception e) {
        System.err.append(name).append("-> ").append(e.getClass().getSimpleName()).append(": ").println(e.message)
    }
}

private static showAttributes(mbsc, ObjectName objName, String ... names) {
    def attrsList=mbsc.getAttributes(objName, names)
    System.out.println(objName)
    for (def attr in attrsList) {
        def value=attr.value
        if ((value instanceof Number) || (value instanceof String) || (value instanceof Boolean)) {
            System.out.append('\t').append(attr.name).append(": ").println(value)
        }
    }
    
    attrsList
}

private static void log(msg) {
    System.out.append("\t>>> ").println(msg)
}
