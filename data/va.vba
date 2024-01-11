Function ParseData() As Object
    Dim ws As Worksheet
    Set ws = ThisWorkbook.Sheets("语料")
    
    Dim lastColumn As Integer
    lastColumn = ws.Cells(3, ws.Columns.Count).End(xlToLeft).Column

    ' 创建一个字典来存储所有数据
    Dim dataDict As Object
    Set dataDict = CreateObject("Scripting.Dictionary")

    Dim i As Integer
    For i = 1 To lastColumn Step 3
        Dim title As String
        title = ws.Cells(1, i).Value
        
        ' 每个标题下创建一个数组来存储评分数据
        Dim scores As Object
        Set scores = CreateObject("Scripting.Dictionary")
        
        Dim j As Integer
        j = 3
        While ws.Cells(j, i).Value <> ""
            ' 对于每行数据，创建一个新的字典来存储min, max和语料
            Dim score As Object
            Set score = CreateObject("Scripting.Dictionary")
            
            score.Add "min", ws.Cells(j, i).Value
            score.Add "max", ws.Cells(j, i + 1).Value
            score.Add "语料", ws.Cells(j, i + 2).Value
            
            ' 将这个新的字典添加到scores数组
            scores.Add scores.Count, score
            j = j + 1
        Wend
        
        ' 将评分数组添加到数据字典
        dataDict.Add title, scores
    Next i
    
    ' 将字典对象返回
    Set ParseData = dataDict
End Function

Sub MatchAndCopyToClipboard()
    ' 弹出输入框，让用户选择分数列
    Dim scoreRange As Range
    On Error Resume Next
    Set scoreRange = Application.InputBox("请选择分数列（带标题）:", Type:=8)
    If scoreRange Is Nothing Then Exit Sub
    On Error GoTo 0
    
    ' 使用ParseData函数解析数据
    Dim data As Object
    Set data = ParseData()
    
    ' 准备匹配语料
    Dim i As Long, c As Long
    Dim matchedCorpus As String
    Dim corpusArray() As String
    ReDim corpusArray(1 To scoreRange.Rows.Count, 1 To scoreRange.Columns.Count)
    
    ' 遍历用户选择的每一列
    For c = 1 To scoreRange.Columns.Count
        Dim scoreColumn As Range
        Set scoreColumn = scoreRange.Columns(c)
        
        ' 遍历每一行
        For i = 1 To scoreColumn.Rows.Count
            Dim score As Variant
            Dim corpusFound As Boolean
            corpusFound = False
            score = scoreColumn.Cells(i, 1).Value
            
            ' 检查分数是否为数字
            If IsNumeric(score) Then
                score = CDbl(score)
                Dim key As Variant, subkey As Variant
                For Each key In data
                    For Each subkey In data(key)
                        Dim minScore As Double, maxScore As Double
                        minScore = data(key)(subkey)("min")
                        maxScore = data(key)(subkey)("max")
                        ' 检查分数是否在当前区间内
                        If score >= minScore And score < maxScore Then
                            matchedCorpus = data(key)(subkey)("语料")
                            corpusFound = True
                            Exit For
                        End If
                    Next subkey
                    If corpusFound Then Exit For
                Next key
            End If
            
            ' 如果没有找到匹配的语料，设置为空字符串
            If Not corpusFound Then matchedCorpus = ""
            
            ' 将匹配的语料存储在数组中对应位置
            corpusArray(i, c) = matchedCorpus
        Next i
    Next c
    
    ' 将数组转换为一个大的字符串，并复制到剪切板
    Dim clipboardText As String
    clipboardText = ArrayToClipboardText(corpusArray)
    CopyTextToClipboard clipboardText
    
    MsgBox "已经复制到剪切板。", vbInformation
End Sub

' 将二维数组转换为剪切板文本
Function ArrayToClipboardText(arr As Variant) As String
    Dim row As Long, col As Long
    Dim clipboardArray() As String
    ReDim clipboardArray(LBound(arr, 1) To UBound(arr, 1))
    
    For row = LBound(arr, 1) To UBound(arr, 1)
        For col = LBound(arr, 2) To UBound(arr, 2)
            clipboardArray(row) = clipboardArray(row) & arr(row, col) & IIf(col < UBound(arr, 2), vbTab, "")
        Next col
    Next row
    
    ArrayToClipboardText = Join(clipboardArray, vbCrLf)
End Function

' 使用这个函数来复制文本到剪切板
Sub CopyTextToClipboard(ByVal Text As String)
    Dim myData As Object
    Set myData = CreateObject("New:{1C3B4210-F441-11CE-B9EA-00AA006B1A69}")
    myData.SetText Text
    myData.PutInClipboard
End Sub

Sub MapGradesAndCopyToClipboard()
    MatchAndCopyToClipboard
End Sub
