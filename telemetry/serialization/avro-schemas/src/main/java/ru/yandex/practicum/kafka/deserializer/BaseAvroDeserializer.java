package ru.yandex.practicum.kafka.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory;
    private final DatumReader<T> reader;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
            if (data == null) {
                return null;
            }
        try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
            // 1. Создаем BinaryDecoder, который будет читать байты из входного потока
            // Второй аргумент (null) указывает на отсутствие переиспользуемого декодера.
            BinaryDecoder decoder = decoderFactory.binaryDecoder(is, null);

            // 2. Используем reader для чтения данных из decoder и создания объекта T.
            // Первый аргумент (null) указывает на отсутствие объекта для переиспользования.
            return reader.read(null, decoder);

        } catch (IOException e) {
            log.error("Ошибка десериализации данных Avro из топика: {}", topic, e);
            throw new SerializationException("Ошибка десериализации данных Avro из топика: " + topic, e);
        } catch (Exception e) {
            log.error("Неизвестная ошибка десериализации для топика: {}", topic, e);
            throw new SerializationException("Неизвестная ошибка десериализации для топика: " + topic, e);
        }

    }
}
