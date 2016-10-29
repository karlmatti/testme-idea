package com.weirddev.testme.intellij;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiPackageImpl;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.testIntegration.TestFramework;
import com.intellij.testIntegration.createTest.TestGenerator;
import com.intellij.testIntegration.createTest.TestGenerators;

import java.io.File;

/**
 * Date: 10/20/2016
 *
 * @author Yaron Yamin
 */
public class TestMeGeneratorTest extends JavaCodeInsightFixtureTestCase {

    private static final String FILE_HEADER_TEMPLATE = "File Header.java";
    private static final String HEADER_TEMPLATE_REPLACEMENT_TEXT = "/** created by TestMe integration test on MMXVI */";
    private static boolean isHeaderTemplateReplaced=false;
    public void testSimpleClass() throws Exception {
        doTest("com.example.services.impl", "Foo", "FooTest");
    }
    public void testDefaultPackage() throws Exception {
        doTest("", "Foo", "FooTest");
    }

    //TODO TC w/out formatting
    //TODO TC with static, final, primitive, primitive wrapper objects fields.
    // TODO TC fields,params and return types that have generics. lambda params?
    // TODO TC no default c'tor
    // TODO TC overloaded methods
    // TODO TC class inheritance
    // TODO TC inner class
    //TODO TC field/param types with same short names from different packages
    //TODO TC multiple field/param types from the same package
    //TODO TC method overloading
    //TODO TC caret position with <caret>
    //TODO TC different test target dir

    private void doTest(final String packageName, String testSubjectClassName, final String expectedTestClassName) {
        myFixture.copyDirectoryToProject("src", "");
        //PsiTestUtil.addSourceRoot(myFixture.getModule(), myFixture.copyDirectoryToProject("src", "src"));
//        final VirtualFile testDir = myFixture.getTempDirFixture().findOrCreateDir("test");
//        assertNotNull("ref file not found", testDir);
//        System.out.println("project test dir:"+testDir);
//        PsiDirectory testDirectory = PsiJavaDirectoryFactory.getInstance(getProject()).createDirectory(testDir);
//        PsiPackageImpl targetPackage = new PsiPackageImpl(getPsiManager(), packageName);
        final PsiClass fooClass = myFixture.findClass(packageName+(packageName.length()>0?".":"") + testSubjectClassName);
        final PsiDirectory srcDir = fooClass.getContainingFile().getContainingDirectory();
        final PsiPackage targetPackage = JavaDirectoryService.getInstance().getPackage(srcDir);


        CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
            @Override
            public void run() {
                myFixture.openFileInEditor(fooClass.getContainingFile().getVirtualFile());
                PsiElement result = new TestMeGenerator().generateTest(new FileTemplateContext(new FileTemplateDescriptor(CreateTestMeAction.TESTME_WITH_JUNIT4_MOCKITO_JAVA), getProject(),
                        expectedTestClassName,
//                new PsiPackageImpl(getPsiManager(), "im.the.generator"),
                        targetPackage,
                        myModule,
                        srcDir,
//                testDirectory,
//                PsiJavaDirectoryFactory.getInstance(getProject()).createDirectory(myFixture.getTempDirFixture().getFile(".").createChildDirectory(this,"com/example/services/impl")),
//               PsiJavaDirectoryFactory.getInstance(getProject()).createDirectory(testDir),
                        fooClass
                ));


                System.out.println("result:"+result);
                String expectedTestClassFilePath = (packageName.length() > 0 ? (packageName.replace(".", "/") + "/") : "") + expectedTestClassName + ".java";
                myFixture.checkResultByFile(/*"src/"+*/expectedTestClassFilePath,"test/"+expectedTestClassFilePath,false);
            }
        }, CodeInsightBundle.message("intention.create.test"), this);

    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("TestDataPath:"+getTestDataPath());
        assertTrue(new File(getTestDataPath()).exists());
        System.out.println("temp dir path:"+myFixture.getTempDirPath());
        replacePatternTemplateText(FILE_HEADER_TEMPLATE, HEADER_TEMPLATE_REPLACEMENT_TEXT);
    }

    private void replacePatternTemplateText(String templateName, String templateText) {
        if(isHeaderTemplateReplaced){
            return;
        }
        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(getProject());
        FileTemplate headerTemplate = fileTemplateManager.getPattern(templateName);
        System.out.println("headerTemplate:"+headerTemplate);
        System.out.println("Existing header Template text:\n"+headerTemplate.getText());
        System.out.println("Replacing header Template text with:\n"+ templateText);
        headerTemplate.setText(templateText);
        isHeaderTemplateReplaced = true;
    }

    @Override
    protected String getTestDataPath() {
        return "testData/testMeGenerator/"+getTestName(true).replace('$', '/');
    }

    @Override
    protected void tuneFixture(JavaModuleFixtureBuilder moduleBuilder) throws Exception {
        moduleBuilder.addJdk(new File(System.getProperty("java.home")).getParent());
    }
}