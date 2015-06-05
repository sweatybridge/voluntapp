package db;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CodeGeneratorTest {
  
  public static int CODE_LENGTH = 6;
  public static int RADIX = 32;
  public static CodeGenerator generator = new CodeGenerator();
  
  @Test
  public void inviteCodeGeneratorGeneratesUniqueCodesOfSpecifiedLenght() {
    String code = generator.getCode();
    assertTrue(code.length() == 6);
  }
}
