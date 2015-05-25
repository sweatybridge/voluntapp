package db;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class InviteCodeGeneratorTest {
  
  public static int CODE_LENGTH = 6;
  public static int RADIX = 32;
  public static InviteCodeGenerator generator = new InviteCodeGenerator();
  
  @Test
  public void inviteCodeGeneratorGeneratesUniqueCodesOfSpecifiedLenght() {
    String code = generator.getInviteCode();
    assertTrue(code.length() == 6);
  }
}
