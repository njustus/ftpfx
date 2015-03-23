# Constructs an instance of the specified Application class
# Calls the init() method
# Calls the start(javafx.stage.Stage) method
# Waits for the application to finish, which happens when either of the following occur:
    ** the application calls Platform.exit()
    ** the last window has been closed and the implicitExit attribute on Platform is true
#  Calls the stop() method