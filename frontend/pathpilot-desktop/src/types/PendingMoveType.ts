export type PendingMoveType = {
    id: number;
    fromPath: string;
    toPath: string | null;
    fileName: string;
    confidence: number;
    operationType: "MOVE" | "TRASH";
};
