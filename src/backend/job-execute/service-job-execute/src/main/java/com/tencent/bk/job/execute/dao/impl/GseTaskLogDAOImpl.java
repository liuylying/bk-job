/*
 * Tencent is pleased to support the open source community by making BK-JOB蓝鲸智云作业平台 available.
 *
 * Copyright (C) 2021 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-JOB蓝鲸智云作业平台 is licensed under the MIT License.
 *
 * License for BK-JOB蓝鲸智云作业平台:
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.tencent.bk.job.execute.dao.impl;

import com.tencent.bk.job.execute.common.util.JooqDataTypeUtil;
import com.tencent.bk.job.execute.dao.GseTaskLogDAO;
import com.tencent.bk.job.execute.model.GseTaskLogDTO;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.generated.tables.GseTaskLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class GseTaskLogDAOImpl implements GseTaskLogDAO {
    private DSLContext create;

    @Autowired
    public GseTaskLogDAOImpl(@Qualifier("job-execute-dsl-context") DSLContext create) {
        this.create = create;
    }

    @Override
    public GseTaskLogDTO getStepLastExecuteLog(long stepInstanceId) {
        GseTaskLog t = GseTaskLog.GSE_TASK_LOG;
        Record record = create.select(t.STEP_INSTANCE_ID, t.EXECUTE_COUNT, t.START_TIME, t.END_TIME, t.TOTAL_TIME,
            t.STATUS, t.GSE_TASK_ID)
            .from(t)
            .where(t.STEP_INSTANCE_ID.eq(stepInstanceId))
            .orderBy(t.EXECUTE_COUNT.desc())
            .limit(1)
            .fetchOne();
        return extractInfo(record);
    }

    private GseTaskLogDTO extractInfo(Record record) {
        if (record == null) {
            return null;
        }
        GseTaskLogDTO gseTaskLogDTO = new GseTaskLogDTO();
        GseTaskLog t = GseTaskLog.GSE_TASK_LOG;

        gseTaskLogDTO.setStepInstanceId(record.get(t.STEP_INSTANCE_ID));
        gseTaskLogDTO.setExecuteCount(record.get(t.EXECUTE_COUNT));
        gseTaskLogDTO.setStartTime(record.get(t.START_TIME));
        gseTaskLogDTO.setEndTime(record.get(t.END_TIME));
        gseTaskLogDTO.setTotalTime(record.get(t.TOTAL_TIME));
        gseTaskLogDTO.setStatus(record.get(t.STATUS).intValue());
        gseTaskLogDTO.setGseTaskId(record.get(t.GSE_TASK_ID));
        return gseTaskLogDTO;
    }

    @Override
    public void saveGseTaskLog(GseTaskLogDTO gseTaskLog) {
        GseTaskLog t = GseTaskLog.GSE_TASK_LOG;
        create.insertInto(t, t.STEP_INSTANCE_ID, t.EXECUTE_COUNT, t.START_TIME, t.END_TIME, t.TOTAL_TIME, t.STATUS,
            t.GSE_TASK_ID)
            .values(gseTaskLog.getStepInstanceId(),
                gseTaskLog.getExecuteCount(),
                gseTaskLog.getStartTime(),
                gseTaskLog.getEndTime(),
                gseTaskLog.getTotalTime(),
                JooqDataTypeUtil.getByteFromInteger(gseTaskLog.getStatus()),
                gseTaskLog.getGseTaskId())
            .onDuplicateKeyUpdate()
            .set(t.START_TIME, gseTaskLog.getStartTime())
            .set(t.END_TIME, gseTaskLog.getEndTime())
            .set(t.TOTAL_TIME, gseTaskLog.getTotalTime())
            .set(t.STATUS, JooqDataTypeUtil.getByteFromInteger(gseTaskLog.getStatus())).set(t.GSE_TASK_ID,
            gseTaskLog.getGseTaskId())
            .execute();
    }

    @Override
    public GseTaskLogDTO getGseTaskLog(long stepInstanceId, int executeCount) {
        GseTaskLog t = GseTaskLog.GSE_TASK_LOG;
        Record record = create.select(t.STEP_INSTANCE_ID, t.EXECUTE_COUNT, t.START_TIME, t.END_TIME, t.TOTAL_TIME,
            t.STATUS, t.GSE_TASK_ID).from(t)
            .where(t.STEP_INSTANCE_ID.eq(stepInstanceId))
            .and(t.EXECUTE_COUNT.eq(executeCount))
            .fetchOne();
        return extractInfo(record);
    }

    @Override
    public void deleteGseTaskLog(long stepInstanceId, int executeCount) {
        GseTaskLog t = GseTaskLog.GSE_TASK_LOG;
        create.deleteFrom(t).where(t.STEP_INSTANCE_ID.eq(stepInstanceId))
            .and(t.EXECUTE_COUNT.eq(executeCount))
            .execute();
    }

}
