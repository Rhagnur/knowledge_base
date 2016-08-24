/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

import groovy.time.TimeCategory

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
        true
    }

    void afterView() {
        // no-op
    }
}
