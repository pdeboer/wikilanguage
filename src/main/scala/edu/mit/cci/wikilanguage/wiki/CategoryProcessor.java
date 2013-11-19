package edu.mit.cci.wikilanguage.wiki;

import edu.mit.cci.wikilanguage.model.Category;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pdeboer
 *         First created on 19/11/13 at 17:35
 */
public class CategoryProcessor {
    private String lang;
    private Set<String> processed = new HashSet<>();
    private ExecutorService exec = Executors.newFixedThreadPool(40);

    public CategoryProcessor(String lang) {
        this.lang = lang;
    }

    public CategoryProcessor() {
        this("en");
    }

    public void shutdown() {
        exec.shutdown();
    }

    public boolean isProcessed(Category category) {
        synchronized (this) {
            return processed.contains(category.name());
        }
    }

    public void addToQueue(Category element) {
        synchronized (this) {
            if (!isProcessed(element)) {
                processed.add(element.name());
                exec.execute(new Worker(element));
            }
        }
    }


    public void process(String name) {
        addToQueue(new Category(name, lang, -1));
    }

    private class Worker implements Runnable {
        Category category;

        private Worker(Category category) {
            this.category = category;
        }

        public void run() {
            scala.collection.Iterator<Category> i = new CategoryContentProcessor(category, true).call().categories().iterator();
            while (i.hasNext()) {
                addToQueue(i.next());
            }
        }
    }
}
