/*
 * Copyright 2015 Sharmarke Aden.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fitbur.testify.need.docker.callback;

import com.fitbur.testify.need.docker.DockerContainer;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import java.io.Closeable;
import java.io.IOException;
import static java.lang.String.format;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;

/**
 * Pull image callback listener.
 *
 * @author saden
 */
public class PullCallback implements ResultCallback<PullResponseItem> {

    private final DockerContainer container;
    private final CountDownLatch latch;
    private final Logger logger;

    public PullCallback(DockerContainer container, CountDownLatch latch, Logger logger) {
        this.container = container;
        this.latch = latch;
        this.logger = logger;
    }

    @Override
    public void onStart(Closeable closeable) {
        logger.info("Pulling '{}:{}' image", container.value(), container.tag());
    }

    @Override
    public void onNext(PullResponseItem object) {
        String id = "N/A";
        long progress = 0;

        if (object.getId() != null) {
            id = object.getId();
        }
        ResponseItem.ProgressDetail details = object.getProgressDetail();

        if (details != null && (details.getCurrent() != 0 && details.getTotal() != 0)) {
            long current = details.getCurrent();
            long total = details.getTotal();
            double percent = current / (double) total;
            System.out.print(
                    format("\r%1$s %2$s: progress: %3$.2f%%",
                            object.getStatus(), id, percent * 100)
            );

        } else {
            System.out.print(
                    format("\r%1$s %2$s",
                            object.getStatus(), id, progress)
            );
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.info("Pull failed due to: '{}'", throwable.getMessage());
    }

    @Override
    public void onComplete() {
        logger.info("Image '{}:{}' pulled", container.value(), container.tag());
        latch.countDown();
    }

    @Override
    public void close() throws IOException {
        logger.debug("Closing pull of '{}:{}' image", container.value(), container.tag());
    }

}
