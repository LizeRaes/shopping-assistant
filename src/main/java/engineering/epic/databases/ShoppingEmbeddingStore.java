package engineering.epic.databases;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import engineering.epic.models.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class ShoppingEmbeddingStore {

    @Inject
    ShoppingDatabase shoppingDatabase;

    EmbeddingStore<TextSegment> embeddingStore;
    EmbeddingModel embeddingModel;

    public ShoppingEmbeddingStore() {
        embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    }

    public EmbeddingStore<TextSegment> getEmbeddingStore() {
        return embeddingStore;
    }

    public void populateFromDatabase() {
        List<Product> products = shoppingDatabase.getAllProducts();
        for (Product product : products) {

            TextSegment segment = TextSegment.from(product.getDescription(), Metadata.from(Collections.singletonMap("product_name", product.getName())));
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
        }
    }

    // TODO build sth similar
//    public void addFeedback(UserFeedback feedback) {
//        if (feedback == null) {
//            return;
//        }
//        for (AtomicFeedback atomicFeedback : feedback.getAtomicFeedbacks()) {
//            Map<String, String> metaInfo = new HashMap<>();
//            metaInfo.put("percentage_of_people_affected", String.valueOf(atomicFeedback.impact));
//            metaInfo.put("severity_in_percent", String.valueOf(atomicFeedback.severity));
//            metaInfo.put("urgency_in_percent", String.valueOf(atomicFeedback.urgency));
//            metaInfo.put("feedback_type", atomicFeedback.feedbackType.toString());
//            // TODO will be List again after bugfix
//            metaInfo.put("feedback_categories_commaseparated", Arrays.asList(atomicFeedback.category).stream().map(Category::name).collect(Collectors.joining(", ")));
//            metaInfo.put("birthyear", String.valueOf(feedback.birthYear));
//            metaInfo.put("gender", feedback.gender);
//            metaInfo.put("nationality", feedback.nationality);
//
//            TextSegment segment = TextSegment.from(atomicFeedback.feedback, Metadata.from(metaInfo));
//            Embedding embedding = embeddingModel.embed(segment).content();
//            embeddingStore.add(embedding, segment);
//        }
//    }
}
