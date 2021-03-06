﻿/*
	Android File Upload Fix ver. 1.1.0  (Agate Java Script Plugin)
	(Fixes the issue that <input type="file"> does not work on Android 4.4.2. )

	Last modified on 2014.11.07

	Created by ApplusForm.com on 2014.10.30
	Permission is granted to copy, distribute, modify under the terms of ApplusForm License.
	Copyright (C) 2014 ApplusForm.com. All rights reserved.
*/

var fixAFU_buttonInnerHTML = "Dosya Seç";
var fixAFU_fileNameInnerHTML = "Dosya seçilmedi";
var fixAFU_uploadStartMessage = "Please wait...";
var fixAFU_chooseFileForUploadMessage = "Choose file for upload";

function fixAFU_replaceFormSubmit(form) {
    if (form.submit != fixAFU_submit) {
        form.submit = fixAFU_submit;

        if (form.onsubmit == null) {
            form.onsubmit = function (e) {
                this.submit();
                e.preventDefault();
                return false;
            };
        } else if (form.onsubmit != null) {
            form.__oldOnsubmit = form.onsubmit;
            form.onsubmit = function (e) {

                var ret = form.__oldOnsubmit(e);
                if (ret != false && !e.defaultPrevented) {
                    this.submit();
                }
                e.preventDefault();
                return false;
            };
        }
    }
}


function fixAFU_escapeFunctionScript(text) {
    text = text.replace(/\\/gm, "\\\\");
    text = text.replace(/\n/gm, "\\n");
    text = text.replace(/\r/gm, "\\r");
    text = text.replace(/'/gm, "\\'");

    return text;
}

function fixAFU_onCompleteFileUpload(args) {
    if (args.responseCode == 200) {
        //document.getElementsByTagName("html")[0].innerHTML = args.responseValue;
        agate.runScript("caller.setHtml('" + fixAFU_escapeFunctionScript(args.responseValue) + "', '" + fixAFU_lastAction + "')");
    } else {
        alert("error: " + args.responseCode + "\n" + args.errorMessage + "\n" + args.responseValue);
    }
}

function fixAFU_submit() {
    try {

        var formTags = new Array();
        var i;

        {
            var inputTags = this.getElementsByTagName("input");
            var selectTags = this.getElementsByTagName("select");
            var textAreaTags = this.getElementsByTagName("textarea");
            for (i = 0; i < inputTags.length; ++i)
                formTags.push(inputTags[i]);
            for (i = 0; i < selectTags.length; ++i)
                formTags.push(selectTags[i]);
            for (i = 0; i < textAreaTags.length; ++i)
                formTags.push(textAreaTags[i]);

        }

        window.jsi.addEventListener('onComplete', 'fixAFU_onCompleteFileUpload')
        var uploadRequestId = agate.http.createRequestId();
        window.jsi.clearPostFileInfo(uploadRequestId);

        var param = "";
        var count = 0;
        for (i = 0; i < formTags.length; ++i) {
            var inputTag = formTags[i];
            if (inputTag.type == "file") {
                var infoIndex = inputTag.getAttribute("inputInfoIndex");
                if (typeof (infoIndex) == "undefined")
                    continue;
                var inputInfo = window.fixAFU_inputInfos[infoIndex];

                if (typeof (inputInfo.choosenFilePath) == "undefined")
                    inputInfo.choosenFilePath = "";
                agate.http.addPostFileInfo(uploadRequestId, inputInfo.inputTag.name, inputInfo.choosenFilePath);
                count++;
            } else {
                param = param + inputTag.name + "=" + inputTag.value + "|";
            }
        }
        param = param + "_charset_=UTF-8" + "|";

        //alert("file count : " + count);
        window.fixAFU_lastAction = this.action;
        window.jsi.showToast(fixAFU_uploadStartMessage);
        window.jsi.post(uploadRequestId, this.action, param);

    } catch (e) {
        alert(e);
    }
}

function getDataUrlFromAgatePath(path) {
    var ext = path.split('.').pop();
    var dataUrl = "data:";
    if (ext.lastIndexOf('/') < 0) {
        ext = ext.toLowerCase();

        if (ext == "png" || ext == "jpg") {
            dataUrl += "image/" + ext;
        }
    }
    dataUrl += ";base64," + window.jsi.readImage(path);

    return dataUrl;
}

// map <INPUT type="file" id="file1"   <IMG id="img1" 
function getImgElementForInputTag(inputTag) {
    // imgRecipe -> imgRecipeIMG
    if (inputTag.id == "file1")
        return document.getElementById("img1");
    if (inputTag.id == "file2")
        return document.getElementById("img2");

    return null;
}

function fixAFU_base64Image(base64) {
    console.log(base64);
}

function fixAFU_onFileChoose(choosenFilePath) { /// bahri

    console.log("tetiklendi");
    
    try {
        if (typeof (choosenFilePath) == "undefined" || choosenFilePath == null || choosenFilePath == "")
            return;

        window.jsi.removeEventListener("onComplete", "fixAFU_onFileChoose");

        var inputInfo = window.fixAFU_inputInfos[window.fixAFU_lastInputInfo];

        var fileName = choosenFilePath.substring(choosenFilePath.lastIndexOf('/') + 1);
        inputInfo.patchedTitle.innerHTML = fileName;
        inputInfo.choosenFilePath = choosenFilePath;

        console.log(inputInfo);

        window.jsi.readImage(choosenFilePath);

        // image preview 
        var img = getImgElementForInputTag(inputInfo.inputTag);
        if (img != null)
            img.setAttribute("src", getDataUrlFromAgatePath(choosenFilePath));

        fixAFU_replaceFormSubmit(inputInfo.inputTag.form);

    } catch (e) {
        alert(e);
    }
};

function fixAndroidFileUpload() { /// bahri
    
    var version = navigator.userAgent.match(/Android [\d+\.]{3,5}/)[0].replace('Android ','');

    if (version != "4.4" && version != "4.4.1" && version != "4.4.2")
        return;

    if (typeof(window.fixAFU_inputInfos) == "undefined")
        window.fixAFU_inputInfos = new Array();


    var inputTags = document.getElementsByTagName("input");

    var i;
    for (i = 0; i < inputTags.length; ++i) {
        var inputTag = inputTags[i];

        if (inputTag.type == "file" && inputTag.style.display != "none") {
            if (inputTag.hasAttribute("inputInfoIndex"))
                continue;

            var infoIndex = window.fixAFU_inputInfos.length;
            inputTag.setAttribute("inputInfoIndex", infoIndex);

            inputTag.style.display = "none";

            var patchedButton = document.createElement("button");
            patchedButton.setAttribute("inputInfoIndex", infoIndex);
            patchedButton.type = "button";
            patchedButton.innerHTML = fixAFU_buttonInnerHTML;
            inputTag.parentNode.insertBefore(patchedButton, inputTag);
            patchedButton.form = inputTag.form;


            var patchedTitle = document.createElement("span");
            patchedTitle.setAttribute("inputInfoIndex", infoIndex);
            patchedTitle.innerHTML = fixAFU_fileNameInnerHTML;
            inputTag.parentNode.insertBefore(patchedTitle, inputTag);

            patchedButton.onclick = function (e) {
                window.fixAFU_lastInputInfo = e.target.getAttribute("inputInfoIndex");
                console.log("bahri");
                window.jsi.selectImage();     
                window.jsi.addEventListener("onComplete", "fixAFU_onFileChoose");          
            };

            var inputInfo = new Object();
            window.fixAFU_inputInfos.push(inputInfo);
            inputInfo.inputTag = inputTag;
            inputInfo.patchedTitle = patchedTitle;
            inputInfo.patchedButton = patchedButton;

        }
    }

    console.log( window.fixAFU_inputInfos );

}


try {
    fixAndroidFileUpload()
} catch(e)
{
	alert(e);
}


