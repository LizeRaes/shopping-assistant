package engineering.epic.databases;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
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
}
