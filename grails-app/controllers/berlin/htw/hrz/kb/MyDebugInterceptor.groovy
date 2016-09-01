/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

import groovy.time.TimeCategory

import java.nio.file.Paths

/**
 * Only for the purpose of testing and debugging, to get the time each method of the controller needs to be executed
 */
class MyDebugInterceptor {
    Date start, stop

    MyDebugInterceptor() {
        matchAll()
    }

    boolean before() {
        start = new Date()
        println("\nStart of action: '${actionName}'...")
        true
    }

    boolean after() {
        stop = new Date()
        println("End of action: ${actionName}, time needed: ${TimeCategory.minus(stop, start)}")
        String path = System.getProperty('user.home') + File.separator + "kbjan" + File.separator
        if (! new File(path).exists()) { new File(path).mkdirs() }
        File logFile = new File(path+'perfomance.log')
        logFile << "${new Date().format('yyyy-MM-dd HH:mm:ss')} ${actionName}\t\t${TimeCategory.minus(stop, start)} needed\n"
        true
    }

    void afterView() {
        // no-op
    }
}
