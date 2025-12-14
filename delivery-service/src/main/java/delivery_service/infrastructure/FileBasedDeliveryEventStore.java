package delivery_service.infrastructure;

import delivery_service.application.DeliveryEventStore;
import delivery_service.domain.*;
import io.vertx.core.json.JsonArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileBasedDeliveryEventStore implements DeliveryEventStore {
    static Logger logger = Logger.getLogger("[DeliveryEventStore]");

    private static final String DELIVERY_PREFIX = "delivery-";

    /* event store file */
    static final String DELIVERY_EVENT_STORE_PATH = System.getProperty("user.dir") + File.separator;

    private final String deliveryEventStoreFile;
    private final HashMap<Integer, DeliveryEvent> events;

    public FileBasedDeliveryEventStore(final String fileName) {
        this.deliveryEventStoreFile = DELIVERY_EVENT_STORE_PATH + fileName;
        this.events = new HashMap<>();
        this.initFromDB();
    }

    @Override
    public void storeDeliveryEvent(final DeliveryEvent event) {
        this.events.put(this.events.size(), event);
        this.saveOnDB();
    }

    @Override
    public Map<DeliveryId, List<DeliveryEvent>> retrieveDeliveryEvents() {
        return this.events.values().stream().collect(Collectors.groupingBy(DeliveryEvent::id));
    }

    @Override
    public DeliveryId getNextId() {
        return new DeliveryId(DELIVERY_PREFIX + this.events.size());
    }

    private void initFromDB() {
        try {
            var eventStore = new BufferedReader(new FileReader(this.deliveryEventStoreFile));
            var sb = new StringBuilder();
            while (eventStore.ready()) {
                sb.append(eventStore.readLine()).append("\n");
            }
            eventStore.close();
            this.events.putAll(DeliveryJsonConverter.fromJson(new JsonArray(sb.toString())));
        } catch (Exception ex) {
            logger.info("DB not found, creating an empty one.");
            saveOnDB();
        }
    }

    private void saveOnDB() {
        try {
            JsonArray list = new JsonArray();
            for (final Map.Entry<Integer, DeliveryEvent> entry: this.events.entrySet()) {
                list.add(DeliveryJsonConverter.toJson(entry.getKey(), entry.getValue()));
            }
            var eventStore = new FileWriter(this.deliveryEventStoreFile);
            eventStore.append(list.encodePrettily());
            eventStore.flush();
            eventStore.close();
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
    }
}
