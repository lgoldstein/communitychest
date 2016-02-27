Option Explicit
' Reads an input text file line by line trying to perform a match
' Once a match is found, it inserts the contents of another file
' before or after the matched line - according to parameter, and so on
' Written by Lyor G.

Dim objFSO, fHandle, insertBefore, outputBuffer
Dim strTextFile, strInputData, strInputLine, arrInputLines
Dim strMatchText, strInsertFile, strInsertData
Dim lngCharPosition, lngLineCounter
CONST ForReading = 1, ForWriting = 2

'Create a File System Object
Set objFSO = CreateObject("Scripting.FileSystemObject")

'''''''''' Get arguments ''''''''''
If Wscript.Arguments.Count <> 4 Then
	Wscript.echo "You must provide a file name, match text, replacement file and position (BEFORE/AFTER): " & vbCrLf & "start /w " & Wscript.ScriptFullName & " SomeFile.txt findMe InsertMe.txt" & vbCrLf
	Wscript.Quit 1
End If

' Input text file
strTextFile = Wscript.Arguments(0)
If Not objFSO.FileExists(strTextFile) Then
	Wscript.echo Wscript.Arguments(0) & " input file does not exist (or is not a file)." & vbCrLf
	Wscript.Quit 1
End If

' Text to match
strMatchText = Wscript.Arguments(1)

' Insertion data file
strInsertFile = Wscript.Arguments(2)
If Not objFSO.FileExists(strInsertFile) Then
	Wscript.echo Wscript.Arguments(2) & " insertion data file does not exist (or is not a file)." & vbCrLf
	Wscript.Quit 1
End If

' Resolve insertion mode
If StrComp(Wscript.Arguments(3), "BEFORE") = 0 Then
	insertBefore = True
ElseIf StrComp(Wscript.Arguments(3), "AFTER") = 0 Then
	insertBefore = False
Else
	Wscript.echo "Unknown insertion position: " & Wscript.Arguments(3) & " - muse be BEFORE or AFTER" & vbCrLf
End If

'Open the text file and split it into lines
Set fHandle = objFSO.OpenTextFile(strTextFile,ForReading)
strInputData = fHandle.ReadAll
fHandle.Close

'Split the text file into lines
arrInputLines = Split(strInputData,vbCrLf)

' Read the insertion data as a whole
Set fHandle = objFSO.OpenTextFile(strInsertFile,ForReading)
strInsertData = fHandle.ReadAll
fHandle.Close

'Step through the lines and build the output buffer
lngLineCounter = 0
outputBuffer = ""
For Each strInputLine in arrInputLines
	lngLineCounter = lngLineCounter + 1
	lngCharPosition = Instr(1, strInputLine, strMatchText, vbBinaryCompare)
	' If found a match then insert the data according to the insertion mode
	If lngCharPosition <> 0 Then
		If insertBefore Then
			outputBuffer = outputBuffer & strInsertData & vbCrLf & strInputLine & vbCrLf
		Else
			outputBuffer = outputBuffer & strInputLine & vbCrLf & strInsertData & vbCrLf 
		End If
	Else
		outputBuffer = outputBuffer & strInputLine & vbCrLf
	End If
Next

Set fHandle = objFSO.OpenTextFile(strTextFile,ForWriting)
fHandle.Write outputBuffer
fHandle.Close

'Cleanup
Set fHandle = Nothing
Set objFSO = Nothing