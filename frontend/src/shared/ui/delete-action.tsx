import { useState } from 'react'
import { AlertDialog } from 'radix-ui'
import { Trash2 } from 'lucide-react'
import { Button } from './button'
import { Notice } from './common'

export function DeleteAction({ name, remove, onDeleted }: { name: string; remove: () => Promise<unknown>; onDeleted: () => void }) {
  const [open, setOpen] = useState(false)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  async function confirm() { if (pending) return; setPending(true); setError(null); try { await remove(); setOpen(false); onDeleted() } catch (failure) { setError(failure) } finally { setPending(false) } }
  return <AlertDialog.Root open={open} onOpenChange={value => { if (!pending) { setError(null); setOpen(value) } }}><AlertDialog.Trigger asChild><Button variant="outline"><Trash2 aria-hidden="true" />Delete</Button></AlertDialog.Trigger><AlertDialog.Portal><AlertDialog.Overlay className="dialog-overlay" /><AlertDialog.Content className="dialog-content" onEscapeKeyDown={event => { if (pending) event.preventDefault() }}><AlertDialog.Title asChild><h2>Delete “{name}”?</h2></AlertDialog.Title><AlertDialog.Description asChild><p>This permanently removes this item. You cannot undo this action.</p></AlertDialog.Description>{!!error && <Notice error={error} />}<div className="actions"><AlertDialog.Cancel asChild><Button variant="outline" disabled={pending}>Cancel</Button></AlertDialog.Cancel><Button variant="destructive" disabled={pending} onClick={confirm}>{pending ? 'Deleting…' : 'Delete permanently'}</Button></div></AlertDialog.Content></AlertDialog.Portal></AlertDialog.Root>
}
