package net.bytebuddy.instrumentation.method.bytecode.stack.member;

import net.bytebuddy.instrumentation.Instrumentation;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackManipulation;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.test.utility.MockitoRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class MethodReturnTest {

    private final Class<?> type;

    private final int opcode;

    private final int sizeChange;

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private MethodVisitor methodVisitor;

    @Mock
    private TypeDescription typeDescription;

    @Mock
    private Instrumentation.Context instrumentationContext;

    public MethodReturnTest(Class<?> type, int opcode, int sizeChange) {
        this.type = type;
        this.opcode = opcode;
        this.sizeChange = sizeChange;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {void.class, Opcodes.RETURN, 0},
                {Object.class, Opcodes.ARETURN, 1},
                {Object[].class, Opcodes.ARETURN, 1},
                {long.class, Opcodes.LRETURN, 2},
                {double.class, Opcodes.DRETURN, 2},
                {float.class, Opcodes.FRETURN, 1},
                {int.class, Opcodes.IRETURN, 1},
                {char.class, Opcodes.IRETURN, 1},
                {short.class, Opcodes.IRETURN, 1},
                {byte.class, Opcodes.IRETURN, 1},
                {boolean.class, Opcodes.IRETURN, 1},
        });
    }

    @Before
    public void setUp() throws Exception {
        when(typeDescription.isPrimitive()).thenReturn(type.isPrimitive());
        when(typeDescription.represents(type)).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {
        verifyZeroInteractions(instrumentationContext);
    }

    @Test
    public void testVoidReturn() throws Exception {
        StackManipulation stackManipulation = MethodReturn.returning(typeDescription);
        assertThat(stackManipulation.isValid(), is(true));
        StackManipulation.Size size = stackManipulation.apply(methodVisitor, instrumentationContext);
        assertThat(size.getSizeImpact(), is(-1 * sizeChange));
        assertThat(size.getMaximalSize(), is(0));
        verify(methodVisitor).visitInsn(opcode);
        verifyNoMoreInteractions(methodVisitor);
    }
}
