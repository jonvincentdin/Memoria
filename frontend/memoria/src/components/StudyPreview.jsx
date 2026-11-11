import { useState } from 'react'

export default function StudyPreview() {
  const [tab, setTab] = useState('Notes')
  const [answer, setAnswer] = useState(null)
  return <div className="study-preview">
    <div className="preview-heading"><span className="preview-dot" /> Your study space <span className="preview-caption">A little preview</span></div>
    <div className="preview-tabs" role="tablist" aria-label="Explore the study preview">
      {['Notes', 'Reviewer', 'Quiz'].map((item) => <button key={item} id={`tab-${item}`} role="tab" aria-selected={tab === item} aria-controls="study-panel" tabIndex={tab === item ? 0 : -1} onClick={() => { setTab(item); setAnswer(null) }} onKeyDown={(event) => {
        const tabs = ['Notes', 'Reviewer', 'Quiz']
        let index = tabs.indexOf(tab)
        if (event.key === 'ArrowRight') index = (index + 1) % tabs.length
        else if (event.key === 'ArrowLeft') index = (index + tabs.length - 1) % tabs.length
        else if (event.key === 'Home') index = 0
        else if (event.key === 'End') index = tabs.length - 1
        else return
        event.preventDefault(); setTab(tabs[index]); setAnswer(null); document.getElementById(`tab-${tabs[index]}`).focus()
      }}>{item}</button>)}
    </div>
    <div className="preview-content" id="study-panel" role="tabpanel" aria-labelledby={`tab-${tab}`} tabIndex="0">
      <div className="subject-label">Biology / The basics</div>
      <h3>Small cells. Big ideas.</h3>
      {tab === 'Notes' && <><p>Every living thing begins with a cell. Think of it as a tiny workspace, with each part doing its own job.</p><div className="note-excerpt"><span className="note-heading">Remember this</span><p>The <mark>mitochondria</mark> produce most of the energy a cell needs.</p></div><div className="note-bottom"><span>One idea at a time.</span><svg width="40" height="22" viewBox="0 0 40 22" fill="none" aria-hidden="true"><path d="M2 16c10-13 15 9 26-7m-6 0 7-1-1 7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/></svg></div></>}
      {tab === 'Reviewer' && <><p>A quick recap, in your own words.</p><dl className="reviewer-list"><div><dt>Nucleus</dt><dd>Stores the cell's genetic instructions.</dd></div><div><dt>Mitochondria</dt><dd>Produce energy the cell can use.</dd></div><div><dt>Cell membrane</dt><dd>Controls what enters and leaves.</dd></div></dl></>}
      {tab === 'Quiz' && <><p>Which part produces most of the cell's energy?</p><div className="quiz-answers">{['Nucleus', 'Mitochondria', 'Cell membrane'].map((option) => <button key={option} type="button" className={answer === option ? 'answer-selected' : ''} aria-pressed={answer === option} onClick={() => setAnswer(option)}>{option}<span aria-hidden="true">{answer === option ? '✓' : '→'}</span></button>)}</div><p className="quiz-feedback" role="status">{answer ? answer === 'Mitochondria' ? 'Exactly. A little practice goes a long way.' : 'Try again. Look for the cell’s energy producer.' : 'Try a practice question.'}</p></>}
    </div>
  </div>
}
