package co.elastic.apm.android.test.testutils.base;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.ArgumentCaptor;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import co.elastic.apm.android.test.testutils.spans.SpanExporterProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class BaseTest {

    protected List<SpanData> getRecordedSpans(int amountExpected) {
        SpanExporter spanExporter = getSpanExporter();
        List<SpanData> spans = getCapturedSpansOrderedByCreation(spanExporter, amountExpected);
        assertEquals(amountExpected, spans.size());

        return spans;
    }

    @SuppressWarnings("unchecked")
    private List<SpanData> getCapturedSpansOrderedByCreation(SpanExporter spanExporter, int amountExpected) {
        List<SpanData> spans = new ArrayList<>();
        ArgumentCaptor<List<SpanData>> captor = ArgumentCaptor.forClass(List.class);
        verify(spanExporter, times(amountExpected)).export(captor.capture());
        for (List<SpanData> list : captor.getAllValues()) {
            if (list.size() > 1) {
                // Since we're using SimpleSpanProcessor, each call to SpanExporter.export must contain
                // only one span.
                throw new IllegalStateException();
            }
            spans.add(list.get(0));
        }

        spans.sort(Comparator.comparing(SpanData::getStartEpochNanos));
        return spans;
    }

    protected SpanExporter getSpanExporter() {
        SpanExporterProvider spanExporterProvider = (SpanExporterProvider) RuntimeEnvironment.getApplication();
        return spanExporterProvider.getSpanExporter();
    }

    protected String getClassSpanName(Class<?> theClass, String suffix) {
        return theClass.getName() + suffix;
    }

    protected SpanData getRecordedSpan() {
        return getRecordedSpans(1).get(0);
    }
}
