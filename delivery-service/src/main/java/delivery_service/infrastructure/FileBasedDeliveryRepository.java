package main.java.delivery_service.infrastructure;

import common.hexagonal.Adapter;
import delivery_service.application.DeliveryAlreadyPresentException;
import delivery_service.application.DeliveryNotFoundException;
import delivery_service.application.DeliveryRepository;
import delivery_service.application.InvalidDeliveryIdException;
import delivery_service.domain.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.logging.Logger;

/**
 * 
 * Games Repository
 * 
 */
@Adapter
public class FileBasedDeliveryRepository implements DeliveryRepository {
	static Logger logger = Logger.getLogger("[DeliveryDB]");

	private static final String DELIVERY_PREFIX = "delivery-";

	/* db file */
	static final String DB_DELIVERIES = "deliveries.json";

	private final HashMap<DeliveryId, Delivery> deliveries;

	public FileBasedDeliveryRepository() {
		this.deliveries = new HashMap<>();
		this.initFromDB();
	}

	@Override
	public DeliveryId getNextId() {
		return new DeliveryId(DELIVERY_PREFIX + (this.deliveries.keySet().stream()
				.map(deliveryId -> Integer.valueOf(deliveryId.id().split("^" + DELIVERY_PREFIX)[1]))
				.max(Integer::compareTo).orElse(-1) + 1));
	}

	@Override
	public void addDelivery(final Delivery delivery) throws InvalidDeliveryIdException, DeliveryAlreadyPresentException {
		if (!delivery.getId().id().matches("^" + DELIVERY_PREFIX + "\\d+$")) {
			throw new InvalidDeliveryIdException();
		}
		if (this.isPresent(delivery.getId())) {
			throw new DeliveryAlreadyPresentException();
		}
		this.deliveries.put(delivery.getId(), delivery);
		this.saveOnDB();
	}

	@Override
	public boolean isPresent(final DeliveryId deliveryId) {
		return this.deliveries.containsKey(deliveryId);
	}

	@Override
	public Delivery getDelivery(final DeliveryId deliveryId) throws DeliveryNotFoundException {
		if (!this.deliveries.containsKey(deliveryId)) {
			throw new DeliveryNotFoundException();
		}
		return this.deliveries.get(deliveryId);
	}

	@Override
	public void updateDeliveryState(final DeliveryId deliveryId, final DeliveryState deliveryState)
			throws DeliveryNotFoundException {
		if (!this.deliveries.containsKey(deliveryId)) {
			throw new DeliveryNotFoundException();
		}
		final Delivery delivery = this.deliveries.get(deliveryId);
		delivery.updateDeliveryState(deliveryState);
		this.deliveries.replace(deliveryId, delivery);
		this.saveOnDB();
	}

	@Override
	public Collection<Delivery> getAllDeliveries() {
		return this.deliveries.values();
	}

	private void initFromDB() {
		try {
			var deliveriesDB = new BufferedReader(new FileReader(DB_DELIVERIES));
			var sb = new StringBuilder();
			while (deliveriesDB.ready()) {
				sb.append(deliveriesDB.readLine()).append("\n");
			}
			deliveriesDB.close();
			var array = new JsonArray(sb.toString());
			for (int i = 0; i < array.size(); i++) {
				final Delivery delivery = DeliveryJsonConverter.fromJson(array.getJsonObject(i));
				this.deliveries.put(delivery.getId(), delivery);
			}
		} catch (Exception ex) {
			logger.info("DB not found, creating an empty one.");
			saveOnDB();
		}
	}

	private void saveOnDB() {
		try {
			JsonArray list = new JsonArray();
			for (final Delivery delivery: this.deliveries.values()) {
				list.add(DeliveryJsonConverter.toJson(
						delivery.getDeliveryDetail(),
						Optional.of(delivery.getDeliveryStatus().getState())
				));
			}
			var usersDB = new FileWriter(DB_DELIVERIES);
			usersDB.append(list.encodePrettily());
			usersDB.flush();
			usersDB.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
