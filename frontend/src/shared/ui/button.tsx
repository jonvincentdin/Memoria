import type { ComponentProps } from 'react'
import { Slot } from 'radix-ui'
import { cva, type VariantProps } from 'class-variance-authority'

const styles = cva('button', { variants: { variant: { default: 'button-primary', outline: 'button-outline', ghost: 'button-ghost', destructive: 'button-danger' }, size: { default: '', small: 'button-small', icon: 'button-icon' } }, defaultVariants: { variant: 'default', size: 'default' } })

/** shadcn/ui's Radix Slot composition, adapted to Memoria's design tokens. */
export function Button({ asChild = false, variant, size, className = '', type = 'button', ...props }: ComponentProps<'button'> & VariantProps<typeof styles> & { asChild?: boolean }) {
  const Component = asChild ? Slot.Root : 'button'
  return <Component data-slot="button" type={asChild ? undefined : type} className={styles({ variant, size, className })} {...props} />
}
