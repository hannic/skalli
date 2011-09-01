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
package org.eclipse.skalli.selenium.pageobjects.ext.editform;

import java.util.concurrent.TimeUnit;

import org.eclipse.skalli.selenium.pageobjects.ext.AbstractExtensionEditForm;
import org.eclipse.skalli.selenium.pageobjects.ext.util.PositionProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * This is the extension edit form for the basics extension
 */
public class BasicsExtensionEditForm extends AbstractExtensionEditForm {
    public BasicsExtensionEditForm(WebDriver driver) {
        super(driver, PositionProvider.getPositionNumberOfExtensionsEditForm(driver,
                PositionProvider.getBascisExtensionEditFormName()));
    }

    @Override
    protected boolean isExtensionContentDisplayed() {
        return getProjectId().isDisplayed() && getDisplayName().isDisplayed() && getShortName().isDisplayed()
                && getDescriptionArea().isDisplayed() && getProjectTemplate().isDisplayed()
                && getProjectTemplateSelectionButton().isDisplayed() && getParentProject().isDisplayed()
                && getParentProjectSelectionButton().isDisplayed() && getProjectPhase().isDisplayed()
                && getProjectPhaseSelectionButton().isDisplayed() && getDeletedCheckBox().isDisplayed();
    }

    public void sendKeysToProjectIdField(String text) {
        getProjectId().sendKeys(text);
    }

    public void sendKeysToDisplayNameField(String text) {
        getDisplayName().sendKeys(text);
    }

    public void sendKeysToShortNameField(String text) {
        getShortName().sendKeys(text);
    }

    public void sendKeysToDescriptionAreaField(String text) {
        getDescriptionArea().sendKeys(text);
    }

    //TODO find a safe way to send keys to a field with a selection button
    public void sendKeysToProjectTemplateField(String text, boolean click) {
        getProjectTemplate().sendKeys(text + "\n");

        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

        if (click) {
            getProjectTemplate().click();
        }
    }

    //TODO find a safe way to send keys to a field with a selection button
    public void sendKeysToParentProjectField(String text, boolean click) {
        getParentProject().sendKeys(text + "\n");

        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

        if (click) {
            getParentProject().click();
        }
    }

    //TODO find a safe way to send keys to a field with a selection button
    public void sendKeysToProjectPhaseField(String text, boolean click) {
        getProjectPhase().sendKeys(text + "\n");

        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

        if (click) {
            getProjectPhase().click();
        }
    }

    public void clickTemplateSelectionButton() {
        getProjectTemplateSelectionButton().click();
    }

    public void clickParentProjectSelectionButton() {
        getParentProjectSelectionButton().click();
    }

    public void clickProjectPhaseSelectionButton() {
        getProjectPhaseSelectionButton().click();
    }

    public void checkDeletedCheckBox(boolean checked) {
        WebElement deletedCheckBox = getDeletedCheckBox();

        if (checked && !deletedCheckBox.isSelected()) {
            deletedCheckBox.click();
        }

        if (!checked && deletedCheckBox.isSelected()) {
            deletedCheckBox.click();
        }
    }

    public String getProjectIdFieldContent() {
        return getProjectId().getAttribute("value");
    }

    public String getDisplayNameFieldContent() {
        return getDisplayName().getAttribute("value");
    }

    public String getShortNameFieldContent() {
        return getShortName().getAttribute("value");
    }

    public String getDescriptionAreaFieldContent() {
        return getDescriptionArea().getAttribute("value");
    }

    public String getProjectTemplateFieldContent() {
        return getProjectTemplate().getAttribute("value");
    }

    public String getParentProjectFieldContent() {
        return getParentProject().getAttribute("value");
    }

    public String getProjectPhaseFieldContent() {
        return getProjectPhase().getAttribute("value");
    }

    protected WebElement getProjectId() {
        return driver.findElement(By.xpath(getXPathToProjectId()));
    }

    protected WebElement getDisplayName() {
        return driver.findElement(By.xpath(getXPathToDisplayName()));
    }

    protected WebElement getShortName() {
        return driver.findElement(By.xpath(getXPathToShortName()));
    }

    protected WebElement getDescriptionArea() {
        return driver.findElement(By.xpath(getXPathToDescription()));
    }

    protected WebElement getProjectTemplate() {
        return driver.findElement(By.xpath(getXPathToProjectTemplate()));
    }

    protected WebElement getProjectTemplateSelectionButton() {
        return driver.findElement(By.xpath(getXPathToProjectTemplateSelectionButton()));
    }

    protected WebElement getParentProject() {
        return driver.findElement(By.xpath(getXPathToParentProject()));
    }

    protected WebElement getParentProjectSelectionButton() {
        return driver.findElement(By.xpath(getXPathToParentProjectSelectionButton()));
    }

    protected WebElement getProjectPhase() {
        return driver.findElement(By.xpath(getXPathToProjectPhase()));
    }

    protected WebElement getProjectPhaseSelectionButton() {
        return driver.findElement(By.xpath(getXPathToProjectPhaseSelectionButton()));
    }

    protected WebElement getDeletedCheckBox() {
        return driver.findElement(By.xpath(getXPathToDeletedCheckBox()));
    }

    private String getXPathToProjectId() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 1
                + "]/td[3]/" + "input";
    }

    private String getXPathToDisplayName() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 2
                + "]/td[3]/" + "input";
    }

    private String getXPathToShortName() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 3
                + "]/td[3]/" + "input";
    }

    private String getXPathToDescription() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 4
                + "]/td[3]/" + "textarea";
    }

    private String getXPathToProjectTemplate() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 5
                + "]/td[3]/" + "div/input";
    }

    private String getXPathToProjectTemplateSelectionButton() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 5
                + "]/td[3]/" + "div/div";
    }

    private String getXPathToParentProject() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 6
                + "]/td[3]/" + "div/input";
    }

    private String getXPathToParentProjectSelectionButton() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 6
                + "]/td[3]/" + "div/div";
    }

    private String getXPathToProjectPhase() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 7
                + "]/td[3]/" + "div/div/div/div/div/input";
    }

    private String getXPathToProjectPhaseSelectionButton() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 7
                + "]/td[3]/" + "div/div/div/div/div/div";
    }

    private String getXPathToDeletedCheckBox() {
        return getXPathToExtensionContainer() + "/div/div/div/div/div/div[3]/fieldset/div[2]/div/table/tbody/tr[" + 7
                + "]/td[3]/" + "div/div/div/div/span/input";
    }
}
