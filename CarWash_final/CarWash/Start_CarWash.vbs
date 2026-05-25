Set WshShell = CreateObject("WScript.Shell")
Dim fso
Set fso = CreateObject("Scripting.FileSystemObject")
WshShell.CurrentDirectory = fso.GetParentFolderName(WScript.ScriptFullName)

' First check if the JAR exists
If Not fso.FileExists("target\carwash-billing-1.1-SNAPSHOT.jar") Then
    MsgBox "System file not found! Please run Build_System.ps1 first to build the application.", 16, "System Error"
    WScript.Quit
End If

' Run the Java application completely silently (no black screen)
WshShell.Run "java --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=javafx.base/com.sun.javafx.event=ALL-UNNAMED --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED -jar target\carwash-billing-1.1-SNAPSHOT.jar", 0, False
