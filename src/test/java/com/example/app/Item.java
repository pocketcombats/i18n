package com.example.app;

import com.pocketcombats.i18n.LocalizedString;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.jspecify.annotations.Nullable;

@Entity
public class Item {

    @Id
    @GeneratedValue
    private @Nullable Long id;

    private @Nullable LocalizedString title;

    public @Nullable Long getId() {
        return id;
    }

    // No `@Convert` — that is the point of the test
    public @Nullable LocalizedString getTitle() {
        return title;
    }

    public void setTitle(@Nullable LocalizedString title) {
        this.title = title;
    }
}
