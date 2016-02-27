Option Explicit
' Does a text find and replace operation on a specified file.
' Written by Eric Phelps
' http://www.ericphelps.com

'Force "cscript"
Main
Wscript.Quit 0

Sub Main()
Dim fil 'As Scripting.File
Dim fils 'As Scripting.Files
Dim fol 'As Scripting.Folder
Dim fols 'As Scripting.Folders
Dim fs 'As Scripting.FileSystemObject
Dim strDirectories() 'As String
Dim strFileTypes 'As String
Dim lngCounter 'As Long
Dim strOldText 'As String
Dim strNewText 'As String
Dim strFileName 'As String
Const READONLY = 1
Const HIDDEN = 2
Const SYSTEM = 4
	'''''''''' Get arguments ''''''''''
	Set fs = CreateObject("Scripting.FileSystemObject")
	If Wscript.Arguments.Count < 2 Then
		MsgBox "You must pass a file name, old text, and new text on the command line:" & vbCrLf & "start /w " & Wscript.ScriptFullName & " C:\MYDOCU~1\README.TXT d:\ e:\" & vbCrLf & "Would replace all instances of ""c:\"" with ""d:\"" in ""README.TXT""."
		Wscript.Quit 1
	End If
	strFileName = Wscript.Arguments(0)
	If Not fs.FileExists(strFileName) Then
		MsgBox Wscript.Arguments(0) & " is not a legitimate file name."
		Wscript.Quit 1
	End If
	Set fil = fs.GetFile(strFileName)
	strOldText = Wscript.Arguments(1)
	If Wscript.Arguments.Count = 3 Then
		strNewText = Wscript.Arguments(2)
	Else
		strNewText = ""
	End If
	If Wscript.ScriptFullName <> fil.Path Then
		If ((fil.Attributes And READONLY) = 0) Then
			If ((fil.Attributes And SYSTEM) = 0) Then
				If ((fil.Attributes And HIDDEN) = 0) Then
					ReplaceText fil, strOldText, strNewText
				End If
			End If
		End If
	End If
End Sub

Sub Status (strMessage)
'If the program was run with CSCRIPT, this writes a
'line into the DOS box. If run with WSCRIPT, it does nothing.
Dim ts 'As Scripting.TextStream
Dim fs 'As Scripting.FileSystemObject
Const ForAppending = 8 'Scripting.IOMode
	If Lcase(Right(Wscript.FullName, 12)) = "\cscript.exe" Then 
		Wscript.Echo strMessage
	End If
End Sub

Sub Force(sScriptEng)
'Forces this script to be run under the desired scripting host
'Valid sScriptEng arguments are "wscript" or "cscript"
'If you don't supply a valid name, Force will switch hosts...
	If Lcase(Right(Wscript.FullName, 12)) = "\wscript.exe" Then
		'Running under WSCRIPT
		If Instr(1, Wscript.FullName, sScriptEng, 1) = 0 Then
			'Need to switch to CSCRIPT
			CreateObject("Wscript.Shell").Run "cscript.exe " & Wscript.ScriptFullName
			Wscript.Quit
		End If
	Else
		'Running under CSCRIPT
		If Instr(1, Wscript.FullName, sScriptEng, 1) = 0 Then
			'Need to switch to WSCRIPT
			CreateObject("Wscript.Shell").Run "wscript.exe " & Wscript.ScriptFullName
			Wscript.Quit
		End If
	End If
End Sub

Sub ReplaceText(objScriptingFile, strOldText, strNewText)
Dim ts 'As Scripting.TextStream
Dim strFileText 'As String
Dim strBuffer 'As String
Dim lngCharPosition 'As Long
Const ForReading = 1 'Scripting.IOMode
Const ForWriting = 2 'Scripting.IOMode
	Set ts = objScriptingFile.OpenAsTextStream(ForReading)
	strFileText = ts.ReadAll
	ts.Close
	Set ts = objScriptingFile.OpenAsTextStream(ForReading)
	lngCharPosition = Instr(1, strFileText, strOldText, vbBinaryCompare)
	If lngCharPosition = 0 Then 
		Exit Sub
	End If
	strBuffer = ""
	Status objScriptingFile.Path
	Do While lngCharPosition <> 0
		Status "	..." & Mid(strFileText, lngCharPosition - 10, Len(strOldText) + 20) & "..."
		strBuffer = strBuffer & Left(strFileText, lngCharPosition - 1) & strNewText
		strFileText = Mid(strFileText, lngCharPosition + Len(strOldText))
		lngCharPosition = Instr(1, strFileText, strOldText, vbBinaryCompare)
	Loop
	strBuffer = strBuffer & strFileText
	Set ts = objScriptingFile.OpenAsTextStream(ForWriting)
	ts.Write strBuffer
	ts.Close
	Set ts = Nothing
End Sub



