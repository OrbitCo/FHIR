import {RequestModel} from "./request.model";
import {ExportModel} from "./export.model";

export interface TaskModel {
    queries: RequestModel[];
    output: ExportModel;
    //schedule: ScheduleModel;
}
