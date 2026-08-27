import type { PendingMoveType } from "../types/PendingMoveType";

type PendingMoveProps = PendingMoveType & {
    onApprove: (id: number) => void;
    onDelete: (id: number) => void;
};

function PendingMove({ id, fromPath, toPath, fileName, operationType, onApprove, onDelete }: PendingMoveProps) {
    const relativePath = (path: string) => path.replace(/^.*?\/Users\/[^/]+\//, "");
    const fromFolder = relativePath(fromPath).split("/").slice(0, -1).join("/");
    const targetPath = toPath ? relativePath(toPath) : "Trash duplicate";
    const actionPath = operationType === "NO_MOVE" ? "no suitable location found" : `${fromFolder} → ${targetPath}`;

    return (
        <div className="flex h-[52px] w-full items-center justify-between border-b border-white/50 text-white">
            <div className="min-w-0">
                <p className="truncate text-[13px] font-medium">{fileName}</p>
                <p className="mt-[4px] truncate text-[11px] text-white/70">{actionPath}</p>
            </div>

            <div className="ml-4 flex gap-[8px]">
                <button
                    className="h-[14px] w-[14px] rounded-full bg-white/50 hover:bg-white/70"
                    onClick={() => operationType !== "NO_MOVE" && onApprove(id)}
                ></button>
                <button
                    className="h-[14px] w-[14px] rounded-full bg-white/30 hover:bg-white/50"
                    onClick={() => operationType !== "NO_MOVE" && onDelete(id)}
                ></button>
            </div>
        </div>
    );
}

export default PendingMove;
