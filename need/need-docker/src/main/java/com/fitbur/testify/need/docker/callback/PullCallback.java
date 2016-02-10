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

import com.fitbur.testify.need.NeedContainer;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import java.io.Closeable;
import java.io.IOException;
import static java.lang.String.format;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;

/**
 * Pull value callback listener.
 *
 * @author saden
 */
public class PullCallback implements ResultCallback<PullResponseItem> {

    private final NeedContainer need;
    private final CountDownLatch latch;
    private final Logger logger;

    public PullCallback(NeedContainer need, CountDownLatch latch, Logger logger) {
        this.need = need;
        this.latch = latch;
        this.logger = logger;
    }

    @Override
    public void onStart(Closeable closeable) {
        logger.info("Pulling '{}:{}' image", need.value(), need.version());
    }

    @Override
    public void onNext(PullResponseItem object) {
        String id = "N/A";

        if (object.getId() != null) {
            id = object.getId();
        }

        ResponseItem.ProgressDetail details = object.getProgressDetail();
        String status = object.getStatus();

        if (details != null && (details.getCurrent() != 0 && details.getTotal() != 0)) {
            double current = details.getCurrent();
            double total = details.getTotal();
            double percent = (current / total) * 100;

            System.out.print(format("%1$s %2$s (%3$.2f%%)\r", status, id, percent));

        } else if (status != null && !status.contains("Already exists")) {
            System.out.print(format("%1$s %2$s\r", status, id));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("Pull failed due to: '{}'", throwable.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("\n");
        logger.info("Image '{}:{}' pulled", need.value(), need.version());
        latch.countDown();
    }

    @Override
    public void close() throws IOException {
        logger.debug("Closing pull of '{}:{}' image", need.value(), need.version());
    }

}
