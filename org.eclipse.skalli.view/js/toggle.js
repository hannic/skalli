/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
var req = null;

function displayResult(responseXML) {
  var result = req.responseText.split('&',2);
  var image = document.getElementById("img_" + result[0]);
  var link = document.getElementById("a_" + result[0]);
  if (image && link) {
    if (result[1] == "true") {
      image.src = "/VAADIN/themes/simple/icons/button/fav_yes.png";
      link.title = "Remove from My Favorites";
    } else {
      image.src = "/VAADIN/themes/simple/icons/button/fav_no.png";
      link.title = "Add to My Favorites";
    }
  }
}

function processResponse() {
  if (req.readyState == 4) {
    if (req.status == 200) {
      displayResult(req.responseXML);
    }
    req = getXMLHttpRequest();
    req.onreadystatechange = processResponse;
  }
}

function getXMLHttpRequest() {
  if (window.XMLHttpRequest) {
    return new window.XMLHttpRequest;
  }
  try {
    return new ActiveXObject("Microsoft.XMLHTTP");
  } catch (ex) {
    return null;
  }
}

function toggleFavorite(uuid) {
  req = getXMLHttpRequest();
  if (req != null) {
    req.onreadystatechange = processResponse;
    req.open("GET", "/favorites?action=toggle&project=" + uuid, true);
    req.send();
  } else {
    window.alert("unsupported operation");
  }
}
