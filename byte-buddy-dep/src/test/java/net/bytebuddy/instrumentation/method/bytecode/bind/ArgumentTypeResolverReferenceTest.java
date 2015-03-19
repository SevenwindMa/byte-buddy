package net.bytebuddy.instrumentation.method.bytecode.bind;

import net.bytebuddy.instrumentation.method.ParameterDescription;
import net.bytebuddy.instrumentation.type.TypeDescription;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.*;

public class ArgumentTypeResolverReferenceTest extends AbstractArgumentTypeResolverTest {

    @Mock
    private TypeDescription weakTargetType;

    @Mock
    private TypeDescription dominantTargetType;

    @Mock
    private ParameterDescription weakTargetParameter;

    @Mock
    private ParameterDescription dominantTargetParameter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(weakTargetType.isAssignableFrom(dominantTargetType)).thenReturn(true);
        when(weakTargetType.isAssignableFrom(weakTargetType)).thenReturn(true);
        when(weakTargetType.isAssignableTo(weakTargetType)).thenReturn(true);
        when(dominantTargetType.isAssignableTo(weakTargetType)).thenReturn(true);
        when(weakTargetParameter.getTypeDescription()).thenReturn(weakTargetType);
        when(dominantTargetParameter.getTypeDescription()).thenReturn(dominantTargetType);
    }

    @Test
    public void testMethodWithoutArguments() throws Exception {
        when(source.getParameters()).thenReturn(sourceParameterList);
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution.isUnresolved(), is(true));
        verify(source, atLeast(1)).getParameters();
        verifyZeroInteractions(left);
        verifyZeroInteractions(right);
    }

    @Test
    public void testMethodWithoutMappedArguments() throws Exception {
        when(sourceParameterList.size()).thenReturn(1);
        when(left.getTargetParameterIndex(argThat(describesArgument(0)))).thenReturn(null);
        when(right.getTargetParameterIndex(argThat(describesArgument(0)))).thenReturn(null);
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.AMBIGUOUS));
        verify(source, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
    }

    @Test
    public void testMethodWithoutSeveralUnmappedArguments() throws Exception {
        when(sourceParameterList.size()).thenReturn(3);
        when(left.getTargetParameterIndex(any())).thenReturn(null);
        when(right.getTargetParameterIndex(any())).thenReturn(null);
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.AMBIGUOUS));
        verify(source, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(2)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1, 2))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(2)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1, 2))));
    }

    @Test
    public void testLeftMethodDominantByType() throws Exception {
        when(sourceParameterList.size()).thenReturn(1);
        when(leftParameterList.get(0)).thenReturn(dominantTargetParameter);
        when(left.getTargetParameterIndex(any())).thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        when(rightParameterList.get(1)).thenReturn(weakTargetParameter);
        when(right.getTargetParameterIndex(any())).thenAnswer(new TokenAnswer(new int[][]{{0, 1}}));
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.LEFT));
        verify(source, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
    }

    @Test
    public void testRightMethodDominantByType() throws Exception {
        when(sourceParameterList.size()).thenReturn(1);
        when(leftParameterList.get(0)).thenReturn(weakTargetParameter);
        when(left.getTargetParameterIndex(any())).thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        when(rightParameterList.get(1)).thenReturn(dominantTargetParameter);
        when(right.getTargetParameterIndex(any())).thenAnswer(new TokenAnswer(new int[][]{{0, 1}}));
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.RIGHT));
        verify(source, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
    }

    @Test
    public void testAmbiguousByCrossAssignableType() throws Exception {
        when(sourceParameterList.size()).thenReturn(1);
        when(leftParameterList.get(0)).thenReturn(weakTargetParameter);
        when(left.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        when(rightParameterList.get(0)).thenReturn(weakTargetParameter);
        when(right.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.AMBIGUOUS));
        verify(source, atLeast(1)).getParameters();
        verify(leftMethod, atLeast(1)).getParameters();
        verify(rightMethod, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
    }

    @Test
    public void testAmbiguousByNonCrossAssignableType() throws Exception {
        when(sourceParameterList.size()).thenReturn(1);
        when(leftParameterList.get(0)).thenReturn(dominantTargetParameter);
        when(left.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        when(rightParameterList.get(0)).thenReturn(dominantTargetParameter);
        when(right.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.AMBIGUOUS));
        verify(source, atLeast(1)).getParameters();
        verify(leftMethod, atLeast(1)).getParameters();
        verify(rightMethod, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0))));
    }

    @Test
    public void testAmbiguousByDifferentIndexedAssignableType() throws Exception {
        when(sourceParameterList.size()).thenReturn(2);
        when(leftParameterList.get(0)).thenReturn(dominantTargetParameter);
        when(leftParameterList.get(1)).thenReturn(weakTargetParameter);
        when(left.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}, {1, 1}}));
        when(rightParameterList.get(0)).thenReturn(weakTargetParameter);
        when(rightParameterList.get(1)).thenReturn(dominantTargetParameter);
        when(right.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}, {1, 1}}));
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.AMBIGUOUS));
        verify(source, atLeast(1)).getParameters();
        verify(leftMethod, atLeast(1)).getParameters();
        verify(rightMethod, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1))));
    }

    @Test
    public void testLeftMethodDominantByScore() throws Exception {
        when(sourceParameterList.size()).thenReturn(2);
        when(leftParameterList.get(0)).thenReturn(dominantTargetParameter);
        when(leftParameterList.get(1)).thenReturn(weakTargetParameter);
        when(left.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}, {1, 1}}));
        when(rightParameterList.get(0)).thenReturn(dominantTargetParameter);
        when(right.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.LEFT));
        verify(source, atLeast(1)).getParameters();
        verify(leftMethod, atLeast(1)).getParameters();
        verify(rightMethod, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1))));
    }

    @Test
    public void testRightMethodDominantByScore() throws Exception {
        when(sourceParameterList.size()).thenReturn(2);
        when(leftParameterList.get(0)).thenReturn(dominantTargetParameter);
        when(left.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}}));
        when(rightParameterList.get(0)).thenReturn(dominantTargetParameter);
        when(rightParameterList.get(1)).thenReturn(dominantTargetParameter);
        when(right.getTargetParameterIndex(any(ArgumentTypeResolver.ParameterIndexToken.class)))
                .thenAnswer(new TokenAnswer(new int[][]{{0, 0}, {1, 1}}));
        MethodDelegationBinder.AmbiguityResolver.Resolution resolution =
                ArgumentTypeResolver.INSTANCE.resolve(source, left, right);
        assertThat(resolution, is(MethodDelegationBinder.AmbiguityResolver.Resolution.RIGHT));
        verify(source, atLeast(1)).getParameters();
        verify(leftMethod, atLeast(1)).getParameters();
        verify(rightMethod, atLeast(1)).getParameters();
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(left, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(left, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1))));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(0)));
        verify(right, atLeast(1)).getTargetParameterIndex(argThat(describesArgument(1)));
        verify(right, never()).getTargetParameterIndex(argThat(not(describesArgument(0, 1))));
    }
}
